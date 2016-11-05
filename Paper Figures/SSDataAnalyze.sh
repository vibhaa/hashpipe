plotscript="pubplot.py"
outfile="SSKeysPerBucketDist.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="SSFPDevData.dat"
datafile2="SSFPKeysData.dat"
datafile3="SSTPDevData.dat"
datafile4="SSTPKeysData.dat"
datafile5="SSNTPNFPDevData.dat"
datafile6="SSNTPNFPKeysData.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -l  -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Number of keys contibuting to a bucket" -y "CDF" \
    --bmargin 5.2 --rmargin 4.3 \
    $datafile6 "Neither True Positive Nor False Positive" "1:4" \
    $datafile4 "True Positive" "1:4"\
    $datafile2 "False Positive" "1:4"\
    | gnuplot ; epstopdf $outfile --autorotate=All
