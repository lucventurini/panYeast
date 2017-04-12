package panalysis {

import scala.util.parsing.combinator._

import scala.io.Source
import java.io._

import scala.collection.mutable.{ArrayBuffer => mArray}

import scala.annotation.tailrec

import scala.Console

object Newick {


  case class Node(id: Int, name: String, annots: Map[String,String], length: Double, children: Array[Int], leaves: Array[Int], parent: Int) {
    def isLeaf = {
      children.length == 0
    }

    def getNewickName = {
      "'%s%s%s'".format(this.name, if (this.annots.size > 0) "+" else "", this.annots.map{ case (k,v) => "%s=%s".format(k,v)}.mkString("+"))
    }

    def getDisplayName = {
      "%s%s%s%s%s".format(if (this.isLeaf) Console.GREEN else Console.RED, this.name, Console.RESET, if (this.annots.size > 0) " " else "", this.annots.map{ case (k,v) => "%s=%s".format(k,v)}.mkString(", "))
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  
  class Tree(nodes: mArray[Node], root: Int) {
 
    val leaves = nodes.filter(n => n.children.length == 0).map(n => n.id)

    def getNode(nodeID: Int) = this.nodes(nodeID)
    def getNodes = this.nodes
    def getRoot = this.root

    ///////////////////////////////////////////////////////////////////////////

    def display = {
      var stack = mArray((this.root,0))
      var lineStack = mArray.empty[String]
  
      while (stack.length > 0) {
        val (cNode, level)  = stack.takeRight(1)(0)
        stack.trimEnd(1)
        lineStack.append("%s>%s".format(("          "*level)+"+-%s-".format( if (this.nodes(cNode).length != 0.0) "%1.5f".format(this.nodes(cNode).length) else "-------"), this.nodes(cNode).getDisplayName))
        stack ++= this.nodes(cNode).children.sortBy( c => this.nodes(c).leaves.length).reverse.map( cid => (cid, level+1))
      }

      // Add the upwards pipes if necessary
      lineStack.indices.reverse.foreach{ i =>
        val pipelocs = lineStack(i).indices.filter(c => lineStack(i).charAt(c) == '|' || lineStack(i).charAt(c) == '+')
        if ( i > 0) {
          pipelocs.filter(pl => pl < lineStack(i-1).length).foreach{ pl =>
            if (lineStack(i-1).charAt(pl) == ' '){
              lineStack(i-1) = lineStack(i-1).slice(0,pl) + "|" + lineStack(i-1).slice(pl+1,lineStack(i-1).length)
            } 
          }
        }
      }

      lineStack.foreach(println)

    }

    override def toString = {
      this.nodes.map(n => "%s(%d): %f C->(%s), L->(%s) P->%d".format(n.name, n.id, n.length, n.children.mkString(","), n.leaves.mkString(","), n.parent)).mkString("\n")
    }

    /////////////////////////////////////////////////////////////////////////////

    def isLeaf(nodeID: Int) = {
      this.nodes(nodeID).isLeaf
    }

    /////////////////////////////////////////////////////////////////////////////

    def setNodeName(nodeID: Int, newName: String) = {
      val n = this.nodes(nodeID)
      this.nodes(nodeID) = Node(n.id, newName, n.annots, n.length, n.children, n.leaves, n.parent) 
    }

    /////////////////////////////////////////////////////////////////////////////

    def addNodeAnnot(nodeID: Int, key: String, value: String) = {
      val n = this.nodes(nodeID)
      this.nodes(nodeID) = Node(n.id, n.name, n.annots ++ Map(key -> value), n.length, n.children, n.leaves, n.parent)
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    def getNodeName(nodeID: Int) = {
      this.nodes(nodeID).name
    }

    def getNewickNodeName(nodeID: Int) = {
      this.nodes(nodeID).getNewickName
    }

    def getDisplayNodeName(nodeID: Int) = {
      this.nodes(nodeID).getDisplayName
    }
    /////////////////////////////////////////////////////////////////////////////

    def getNodeLength(nodeID: Int) = {
      this.nodes(nodeID).length
    }

    /////////////////////////////////////////////////////////////////////////////

    def toNewick: String = {
      var nodeStates = Array.fill[Int](this.nodes.length)(-1)

      @tailrec def toNewickHelper(nodeID: Int, newick: String): String = {
        nodeStates(nodeID) += 1
        Debug.message("Current Node: %s(%d), Child node to consider %s(%d), %d -> Parent Node ID%s".format(this.nodes(nodeID).name, nodeID, if(this.nodes(nodeID).children.length > nodeStates(nodeID)) this.nodes(this.nodes(nodeID).children(nodeStates(nodeID))).name else "*", nodeStates(nodeID), this.nodes(nodeID).parent, newick))
        nodeStates(nodeID) match  {
          case childNodeID if (childNodeID < this.nodes(nodeID).children.length)  => {
            nodeStates(nodeID) match {
              case 0 => toNewickHelper(this.nodes(nodeID).children(nodeStates(nodeID)), newick + "(") // We are the first child node here now
              case _ => toNewickHelper(this.nodes(nodeID).children(nodeStates(nodeID)), newick + ",") // We are not the first child node here now
            }
          }
          case _ => {
            this.nodes(nodeID).parent match {
              case -1 => newick + "%s%s%s;".format(if (nodeStates(nodeID) == 0) "" else ") ", this.nodes(nodeID).getNewickName, if (this.nodes(nodeID).length != 0.0) ":%f".format(this.nodes(nodeID).length) else "") // We have reached the root node
              case _  => toNewickHelper(this.nodes(nodeID).parent, newick + "%s%s%s".format( if (nodeStates(nodeID) == 0) "" else ") ", this.nodes(nodeID).getNewickName, if (this.nodes(nodeID).length != 0.0) ":%f".format(this.nodes(nodeID).length) else "")) // We just returned from the last child. Go back to the parent
            }
          }
        }
      }
      toNewickHelper(this.root, "") 
    }

    /////////////////////////////////////////////////////////////////////////////

    def outGroupRoot(outGroup: String, newRootName: String = "outGroupRoot") = {
      val outGroupID = this.nodes(this.nodes.indexWhere( n => {n.name == outGroup})).id
      val outGroupNode = this.nodes(outGroupID)

      var nodes = this.nodes :+ Node(this.nodes.length, newRootName, Map.empty[String,String], 0.0, Array(outGroupID, this.nodes(outGroupID).parent), this.leaves.toArray, -1)

      // Set parent of outGroupNode to new root node
      nodes(outGroupID) =  Node(outGroupNode.id, outGroupNode.name, outGroupNode.annots, outGroupNode.length, outGroupNode.children, outGroupNode.leaves, this.nodes.length)

      @tailrec def reRootHelper(nodes: mArray[Node], nodeID: Int, comeFromNodeID: Int): mArray[Node] = {
        val thisNode = nodes(nodeID)
        val newChildren = if (nodeID == this.root) {
          thisNode.children.filter(c => !nodes(comeFromNodeID).children.contains(c) && c != comeFromNodeID)
        } else {
          thisNode.children.filter(c => !nodes(comeFromNodeID).children.contains(c) && c != comeFromNodeID) :+ thisNode.parent
        }

        nodes(nodeID) = Node(thisNode.id, thisNode.name, thisNode.annots, thisNode.length, newChildren, Array.empty[Int], comeFromNodeID)

        if (nodeID == this.root) {
          nodes
        } else {
          reRootHelper(nodes, thisNode.parent, nodeID)
        }
      }
      // Only way I can figure out to get the leaves right at the moment...
      // Probably superfluous.
      if (this.root == outGroupID) {
        this
      } else {
        Newick.Tree.fromString(new Newick.Tree(reRootHelper(nodes, this.nodes(outGroupID).parent, this.nodes.length), this.nodes.length).toNewick)
      }
    }

    /////////////////////////////////////////////////////////////////////////////

      def topologicalSorting = {
        var status = Array.fill(this.nodes.length)(0)
        @tailrec def topologicalSortingHelper(arr: Array[Int], nodeID: Int): Array[Int] = {
          status(nodeID) match {
            case it if 0 until this.nodes(nodeID).children.length-1 contains it => {
              Debug.message("Going to child %s -> %s".format(this.nodes(nodeID).name, this.nodes(this.nodes(nodeID).children(status(nodeID))).name))
              topologicalSortingHelper(arr :+ this.nodes(nodeID).children(status(nodeID)), this.nodes(nodeID).children(status(nodeID)))
            }
            case _ => {
              this.nodes(nodeID).parent match {
                case -1 => arr
                case _  => {
                  Debug.message("Going to parent %s -> %s".format(this.nodes(nodeID).name, this.nodes(this.nodes(nodeID).parent).name))
                  status(this.nodes(nodeID).parent) = status(this.nodes(nodeID).parent) + 1
                  topologicalSortingHelper(arr, this.nodes(nodeID).parent)
                }
              }
            }
          }
        }

        topologicalSortingHelper(Array(this.root), this.root)
      }

      /////////////////////////////////////////////////////////////////////////////

      def longestPath = {
          // The children include the parent (make undirected)
        def longestPathHelperUnvisitedChildren(nodeID: Int, visited: Array[Int]) = {
          {if (this.nodes(nodeID).parent == -1) {
            this.nodes(nodeID).children
          } else {
              this.nodes(nodeID).children :+ this.nodes(nodeID).parent
          }}.filter(n => ! (visited contains n))
        }

        @tailrec def longestPathHelper(path: Array[Int], visited: Array[Int], distances: Map[Int,Float], longestPath: Array[Int]): Array[Int] = {

          path.length match {
            case 0 => longestPath
            case _ => {
              val nodeID = path.last
              Debug.message("%s(%s)".format(path.mkString("->"), this.getNodeName(nodeID)))
              val children = longestPathHelperUnvisitedChildren(nodeID, visited)
              Debug.message("Visited: %s".format(visited.map(this.getNodeName).mkString(",")))
              Debug.message("Potential: %s".format(children.map(this.getNodeName).mkString(",")))
              val newDistances: Map[Int,Float] = {
                if (visited contains nodeID) {
                  distances
                } else if (path.length > 1) {
                  distances + (nodeID -> (distances(path(path.length-2)) + (this.nodes(nodeID)).length.toFloat))
                } else {
                  distances + (nodeID -> 0.toFloat)
                }
              }

              Debug.message(newDistances.mkString(","))

              val newLongestPath = {
                if (visited contains nodeID) {
                  longestPath
                } else if (newDistances(nodeID) > newDistances(longestPath.last)) {
                  Debug.message("New Longest Path -> %s".format(path.map(this.getNodeName).mkString(",")))
                  path
                } else {
                  longestPath
                }
              }

              val newVisited = {
                if (visited contains nodeID) {
                  visited
                } else {
                  visited :+ nodeID
                }
              }
              Debug.message("")
              children.length match {
                  // Go back to parent in path
                case 0 => longestPathHelper(path.dropRight(1), newVisited, newDistances, newLongestPath)
                  // Go to a child
                case _ => longestPathHelper(path :+ children.last, newVisited, newDistances, newLongestPath)
              }
            }
          }
        }

        val endNode = longestPathHelper(Array(this.root), Array.empty[Int], Map(this.root -> 0.toFloat), Array(this.root)).last
        longestPathHelper(Array(endNode), Array.empty[Int], Map(endNode -> 0.toFloat), Array(endNode))
      }

    /////////////////////////////////////////////////////////////////////////////

    def midPointRoot = {

      val longestPath = this.longestPath
      val distances   = longestPath map this.getNodeLength
      val cumDist     = distances.scanLeft(0.toDouble)(_ + _)
      val midPoint    = distances.reduce(_ + _) / 2.toFloat
      val pivot       = longestPath(cumDist.drop(1).span(_ <= midPoint.toDouble)._1.length -1)

      println((longestPath map this.getNodeName).mkString("->"))
      println(cumDist.mkString("->"))
      println(midPoint)
      println(pivot)

      this.outGroupRoot(this.getNodeName(pivot), "midPointRoot")

    }

    /////////////////////////////////////////////////////////////////////////////

  }

  /////////////////////////////////////////////////////////////////////////////

  // Companion
  object Tree {

    def fromString(tree: String) = {
      val nodes = Newick.read(tree).toArray()
      new Newick.Tree(nodes, 0)
    }

  }

  /////////////////////////////////////////////////////////////////////////////

  def read(text: String) = {
    val parser = new NewickParser(NewickNode.apply)
    parser.read(text)
  }


  /////////////////////////////////////////////////////////////////////////////
  // Copied and Modified from http://codeaffectionate.blogspot.nl/2012/12/newick-tree-format-parser-in-scala.html

  case class NewickNode(name:String, length:Double, children:List[NewickNode]) {
    def display(level:Int=0) {
      printf("%s%s:%.2f\n","  "*level,name,length)
      children.foreach(_.display(level+1))
    }

    ///////////////////////////////////////////////////////////////////////////

    def isLeaf = {
      children.length == 0
    }

    ///////////////////////////////////////////////////////////////////////////

    def toArray(identStart: Int = 0, parent: Int = -1) : mArray[Node] = {
      // I can't make this recursive AND keep the parent node information in here...
      val nodeID = identStart
      var nodes = mArray.empty[Node]
      var childIDs = mArray.empty[Int]
      var count = identStart+1
      this.children.foreach{ c =>
        val dNodes = c.toArray(count, nodeID)
        childIDs append count
        count += dNodes.length
        nodes ++= dNodes
      }
      val leafIDs    = nodes.filter(n => n.children.length == 0).map(n => n.id)
      val nameFields = name.split('+')
      val realName   = nameFields(0)
      val annots: Map[String,String] = if (nameFields.length > 1) nameFields.slice(1,nameFields.length).map(x => x.split('=')).map( arr => arr(0) -> (if (arr.length > 1) arr(1) else "")).toMap else Map.empty[String,String]

      nodes prepend Node(nodeID, realName, annots, this.length, childIDs.toArray, leafIDs.toArray, parent)
      nodes
    }

  }

  /////////////////////////////////////////////////////////////////////////////

  abstract class TreeParser[T] extends JavaTokenParsers {
    val comment = """\[.+?\]"""

    def tree:Parser[T]

    def read(text:String):T = parseAll(tree, text.replaceAll(comment,"")) match {
      case Success(result, next) => result
      case failure => throw new Exception(failure.toString)
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  class NewickParser[T](nf: (String,Double,List[T]) => T) extends TreeParser[T] {
    var nodes: Int  = 0
    var leaves: Int = 0
    
    def tree = subtree <~ ";"
    def children:Parser[List[T]] = "(" ~> repsep(subtree, ",") <~ ")"
    def subtree = children~name~length ^^ { case t~n~l => nf(n,l,t)} | leaf
    def leaf = name~length ^^ {case n~l => nf(n,l,Nil)}
    def name = opt(startsWithNumber | quoted | unquoted) ^^ { _.getOrElse("") }
    def startsWithNumber = """([0-9][^):,]*)""".r ^^ { n => n }
    def unquoted = ident
    def quoted = """'([^']|'')*'""".r  ^^ { _.drop(1).dropRight(1).replace("''","'") }
    def length = opt(":" ~> floatingPointNumber) ^^ { _.getOrElse("0").toDouble }
  }

}

}
