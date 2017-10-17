# Copyright 2017 by Sean Luke
# Licensed under the Academic Free License version 3.0
# See the file "LICENSE" for more information
#
# This R script takes the contatenated results of two regressuion suite
# executions and tests all the apps for statistically significent
# differences in their median best fitness distribution.
#
# Author: Eric O. Scott

setwd("~/NetBeansProjects/ecj/ecj/src/test/regression/")

# Paths of ECJ regression results files to compare.  Change these as needed.
file1 <- "ecj24_regression.csv"
file2 <- "ecj26dev_regression_eb4f0ae.csv"

# P-value to use as error detection threshold.
p <- 0.05

pdf(NULL)
# Load regression test outputs
file1 <- read.csv(file1, header=FALSE, stringsAsFactors=FALSE)
file1$version = "A"
file2 <- read.csv(file2, header=FALSE, stringsAsFactors=FALSE)
file2$version = "B"
data <- rbind(file1, file2)
h <- c("app", "subpop", "best", "gen", "version")
names(data) <- h

# Get the names of all apps in the reference suite
apps <- unique(file1$V1)

print(paste("Comparing regression results for", length(apps), "apps..."))


# Apply a Wilcox test to each app to see if its median best-fitness behavior has changed
x <- lapply(apps, function(a)  wilcox.test(best~version, data=data[data$app == a,])$p.value )
results <- data.frame(cbind(apps, x))
names(results) <- c("app", "p.value")

# Print the apps where the null is rejected after a Bonferroni correction
numTests <- length(apps)
bonferroni <- p/numTests
bad <- results[!is.na(results$p.value),]
bad <- bad[bad$p.value < bonferroni,]
print(paste(nrow(bad), "FAILED REGRESSION TESTS:"))
print(bad)

library(ggplot2)
plotHists <- function(d) {
  appName <- d[1,]$app
  g <- ggplot(d, aes(x=best, group=version, fill=version)) +
    geom_histogram(alpha= 0.7, aes(y=..count..), position="identity") +
    ggtitle(paste("Regression Test for", appName)) +
    ylab("Runs") +
    xlab("Best Fitness")
  dir.create("plots", showWarnings = FALSE)
  outname <- paste("plots/", appName, ".pdf", sep="")
  print(paste("Saving histogram to", outname))
  ggsave(outname, plot=g)
}

z <- sapply(bad$app, function(b) plotHists(data[data$app == b,]))

