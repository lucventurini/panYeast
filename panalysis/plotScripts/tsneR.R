
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.dir <- dirname(sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)]))

source(paste(script.dir, "plotFunctions.R", sep="/"))
source(paste(script.dir, "utils.R", sep="/"))

usage <- function(arg0) {
  printf("%s <task> <args>\n", arg0)
  printf("\n")
  printf("  %s intensity <inFile> <outFile> <intensityFile>\n")
  printf("  %s labels <inFile> <outFile> <labelFile>\n")
  printf("  %s raw <inFile> <outFile> \n")
  printf("")
  printf("  inFile: Tab-separated X,Y points resulting from TSNE. One point per line\n")
  printf("  outFile: Output File\n")
  printf("  intensityFile: For each point, an intensity value\n")
  printf("  labelFile: For each point, a label\n")

}

args = commandArgs(trailingOnly=TRUE)

if (length(args) < 3){
  usage(args[0])
}

task      = args[1]
inFile    = args[2]
annotFile = args[4]
outFile   = args[3]

D <- read.table(inFile, sep="\t")
colnames(D) <- c("x", "y")

if (task == "intensity") {
  I = scan(file=annotFile)
  tsnePlotIntensity(D, I, outFile)
} else if (task == "labels") {
  L = scan(file=annotFile, what=int())
  tsnePlotLabels(D, L, outFile)
} else if (task == "raw") {
  tsnePlot(D, "black", outFile)
}
