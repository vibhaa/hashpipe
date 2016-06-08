#!/usr/bin/env sh

java LossyFlowIdentifier ../Analysis/sample2.csv ../Analysis/SizeOfAllFlowsSample2.csv no 30 > ../Analysis/30/Sam2SingleLookupSizeDifference30.csv

java LossyFlowIdentifier ../Analysis/sample2.csv ../Analysis/SizeOfAllFlowsSample2.csv no 50 > ../Analysis/50/Sam2SingleLookupSizeDifference50.csv

java LossyFlowIdentifier ../Analysis/sample2.csv ../Analysis/SizeOfAllFlowsSample2.csv no 100 > ../Analysis/100/Sam2SingleLookupSizeDifference100.csv

java LossyFlowIdentifier ../Analysis/sample2.csv ../Analysis/SizeOfAllFlowsSample2.csv no 200 > ../Analysis/200/Sam2SingleLookupSizeDifference200.csv

java LossyFlowIdentifier ../Analysis/sample2.csv ../Analysis/SizeOfAllFlowsSample2.csv no 300 > ../Analysis/300/Sam2SingleLookupSizeDifference300.csv

java LossyFlowIdentifier ../Analysis/sample2.csv ../Analysis/SizeOfAllFlowsSample2.csv no 500 > ../Analysis/500/Sam2SingleLookupSizeDifference500.csv



