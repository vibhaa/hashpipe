plotscript="pubplot.py"
outfile="FalseNegvsKSingle.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="Single1500k.dat"
datafile2="Single3000k.dat"
datafile3="Single4500k.dat"
datafile4="SSTPKeysData.dat"
datafile5="SSNTPNFPDevData.dat"
datafile6="SSNTPNFPKeysData.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -l -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Number of heavy hitters (k)" -y "False Negative % per k" \
    --bmargin 5.2 --rmargin 4.3 \
    $datafile1 "m = 1500" "2:4" \
    $datafile2 "m = 3000" "2:4"\
    $datafile3 "m = 4500" "2:4"\
    | gnuplot ; epstopdf $outfile --autorotate=All
