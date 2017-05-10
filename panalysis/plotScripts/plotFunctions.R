library(RColorBrewer)

###############################################################################

tsnePlotLabels <- function(D, labels, outFile) {
  ncolors <- max(labels) - min(labels)
  
  tsnePlot(D, colors, outFile)
}

###############################################################################

tsnePlotIntensity <- function(D, labels, outFile) {
  colors <- sapply(labels, function(x) { rgb(x/max(D$csize),0,0,0.5) })
  tsnePlot(D, colors, outFile)
}

###############################################################################

tsnePlot(D, colors, outFile) {
  pdf(outFile)
  plot(D$x, D$y, col=colors, pch=20, title="", xlab="" ylab="")
  dev.off()
}


###############################################################################
