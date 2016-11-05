plotscript="pubplot.py"
outfile="FriendsFalseNegWithRep.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="Single300DiffRep.dat"
datafile2="Baseline300DiffRep.dat"
datafile3="SS300DiffRep.dat"
datafile4="SS150.dat"
datafile5="CM150.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Number of flows reported" -y "False Negative %"\
    --bmargin 5.2 --rmargin 4.3 \
    $datafile1 "HashPipe" "4:5"\
    $datafile2 "HashParallel" "4:5" \
    $datafile3 "Space Saving" "4:5" \
    | gnuplot ; epstopdf $outfile --autorotate=All
