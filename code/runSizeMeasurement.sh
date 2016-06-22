#!/usr/bin/env sh

java LossyFlowIdentifier ../Analysis/sample1.csv ../Analysis/SizeOfAllFlows.csv no 30 > ../Analysis/30/NoCoalSizeDifference30.csv

java LossyFlowIdentifier ../Analysis/sample1.csv ../Analysis/SizeOfAllFlows.csv no 50 > ../Analysis/50/NoCoalSizeDifference50.csv

java LossyFlowIdentifier ../Analysis/sample1.csv ../Analysis/SizeOfAllFlows.csv no 100 > ../Analysis/100/NoCoalSizeDifference100.csv

java LossyFlowIdentifier ../Analysis/sample1.csv ../Analysis/SizeOfAllFlows.csv no 200 > ../Analysis/200/NoCoalSizeDifference200.csv

java LossyFlowIdentifier ../Analysis/sample1.csv ../Analysis/SizeOfAllFlows.csv no 300 > ../Analysis/300/NoCoalSizeDifference300.csv

java LossyFlowIdentifier ../Analysis/sample1.csv ../Analysis/SizeOfAllFlows.csv no 500 > ../Analysis/500/NoCoalSizeDifference500.csv



