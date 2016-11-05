plotscript="pubplot.py"
outfile="FriendsFalseNeg60.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="Single150.dat"
datafile2="Baseline150.dat"
datafile3="Single60.dat"
datafile4="SS150.dat"
datafile5="SS60.dat"
datafile6="Baseline60.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Memory (in KB)" --xrange "[0:80]" -y "False Negative %" --yrange "[0:25]"\
    --bmargin 5.2 --rmargin 4.3 \
    $datafile5 "Space Saving" "1:4"\
    $datafile6 "HashParallel" "1:4" \
    $datafile3 "HashPipe" "1:4" \
    | gnuplot ; epstopdf $outfile --autorotate=All
