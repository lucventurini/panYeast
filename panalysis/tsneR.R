
tsnePlot <- function(inFile, outFile) {

  D <- read.table(inFile, sep="\t")
  colnames(D) <- c("x", "y", "core", "csize")

  pdf(outFile)
  colors <- sapply(D$core, function(x) { ifelse(x == 1, rgb(1,0,0,0.5), rgb(0,0,0,0.5)) })
  colors <- sapply(D$csize, function(x) { rgb(x/max(D$csize),0,0,0.5) })
  plot(D$x, D$y, col=colors, pch=20)
  dev.off()

}

args = commandArgs(trailingOnly=TRUE)

tsnePlot(args[1], args[2])

#tsnePlot("tsneCount.mat", "tsneCount.pdf")
#tsnePlot("tsneBinary.mat", "tsneBinary.pdf")

#tsnePlot("tsneCountSpecies.mat", "tsneCountSpecies.pdf")
#tsnePlot("tsneBinarySpecies.mat", "tsneBinarySpecies.pdf")

