package panalysis {

import scala.collection.mutable.{SynchronizedMap, HashMap}

class mciNetwork {

  var net = HashMap.empty[Int,HashMap[Int,Double]]
  var edgeCount = 0

  //////////////////////////////////////////////////////////////////////////////

  def get(i: Int, j: Int) = {
    if ( (net contains i) & (net(i) contains j)) {
      net(i)(j)
    } else {
      0.toDouble
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  def setNet(newNet: HashMap[Int,HashMap[Int,Double]], newEdgeCount: Int) = {
    this.net = newNet
    this.edgeCount = newEdgeCount
  }

  //////////////////////////////////////////////////////////////////////////////

  def +=(outNode:Int, inNode:Int, weight:Double) = {
    if (!(net contains outNode)) {
      net += (outNode -> HashMap.empty[Int,Double])
    }
    if (!(net contains inNode)) {
      net += (inNode -> HashMap.empty[Int,Double])
    }
    net(outNode) += (inNode -> weight)
    net(inNode)  += (outNode -> weight)

    edgeCount += 1

  }

  //////////////////////////////////////////////////////////////////////////////

  def getNodeCount = {
     net.keys.size
  }

  //////////////////////////////////////////////////////////////////////////////

  def getEdgeCount = {
    edgeCount
  }

 //////////////////////////////////////////////////////////////////////////////

//  def subnet(ids: Array[Int]) = {
//    var newNet = new mciNetwork
//    newNet.setNet(net.filterKeys( k => ids contains k).map{ case (k,v) =>
//      (k, v.filterKeys(vk => ids contains vk))
//    })
//    newNet
//  }

  //////////////////////////////////////////////////////////////////////////////

}

}
