plotscript="pubplot.py"
outfile="minDistribution.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="HPMin.dat"
datafile2="GlobalMin.dat"
datafile3="Single420c.dat"
datafile4="Single60c.dat"
datafile5="dummy.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript --lx --ly -a "font \"Helvetica,20\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Value of minimum (x)" --xrange ["1:1000"] -y "CCDF (Pr(minimum >= x))"\
    --bmargin 5.2 --rmargin 4.3 \
    $datafile1 "HashPipe Minimum" "1:6" \
    | gnuplot ; epstopdf $outfile --autorotate=All
