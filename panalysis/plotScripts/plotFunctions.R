library(RColorBrewer)
library(hexbin)
library(scales)

###############################################################################

tsnePlotLabels <- function(D, labels, outFile, ...) {
  uLabels <- unique(labels)
  nLabels <- length(uLabels)

  labelMap <- list()
  for (label_i in 0:(nLabels-1)) {
    labelMap[uLabels[[label_i+1]]] = label_i
  }
  print(labelMap)

  mLabels = list()
  for (label_i in (1:length(labels))) {
    mLabels[label_i] <- labelMap[[labels[[label_i]]]]
  }

  cols <- brewer.pal(n=min(nLabels,9), name="Set1")
  pchs = c("+","o","*","-",".","#","%",15,16,17,18)
  labelpch <- sapply((0:(nLabels-1))/9, function(x){pchs[floor(x)+1]})

  print(labelpch)

  plotcolors <- cols[sapply(1:length(labels), function(x) {(mLabels[[x]] %% 9)+1} )]
  plotpch    <- sapply(1:length(labels), function(x) {labelpch[mLabels[[x]]+1]})

  legendString <- lapply(uLabels, toString)
  legendColors <- cols[((0:(nLabels-1)) %% 9) + 1]
  legendpch    <- labelpch[1:nLabels]

  print(legendString)
  print(legendColors)
  print(legendpch)

  tsnePlot(D, plotcolors, outFile, pch=plotpch, labelLegend=TRUE, lls=legendString, llc=legendColors, llp=legendpch, ...)
}

###############################################################################

tsnePlotIntensity <- function(D, labels, outFile, ...) {
  colors <- sapply(labels, function(x) { rgb(x/max(labels),0,0,0.5) })
  tsnePlot(D, colors, outFile, ...)
}

###############################################################################

tsnePlot <- function(D, colors, outFile, pch=21, labelLegend=FALSE, lls=NULL, llc=NULL, llp=NULL, ...) {
  pdf(outFile, title=outFile)
  plot(D$x, D$y, col=colors, pch=pch, xlab="", ylab="", ...)
  if (labelLegend){
    legend("topright", legend=lls, col=llc, pch=llp)
  }
  dev.off()
}


###############################################################################

scatterPlot <- function(X, Y, ...) {
  plot(X, Y, col=alpha("black", 0.2), ...)

}

###############################################################################

hexbinPlot <- function(X,Y, ...) {
  df <- data.frame(x=X, y=Y)
  h <- hexbin(df)
  plot(h, ...)
}
