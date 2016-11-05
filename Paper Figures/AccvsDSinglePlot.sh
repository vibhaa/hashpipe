plotscript="pubplot.py"
outfile="FalseNegvsDSingle.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="Single140c.dat"
datafile2="Single210c.dat"
datafile3="Single350c.dat"
datafile4="Single420c.dat"
datafile5="Single300.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Number of table stages (d)" -y "False Negative %" \
    --bmargin 5.2 --rmargin 4.3 \
    $datafile1 "k = 140, m = 3360" "3:4" \
    $datafile2 "m = 210, m = 5040" "3:4"\
    $datafile3 "m = 350, m = 6720" "3:4"\
    $datafile4 "m = 420, m = 8400" "3:4"\
    | gnuplot ; epstopdf $outfile --autorotate=All
