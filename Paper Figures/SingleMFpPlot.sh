plotscript="pubplot.py"
outfile="FalsePosvsMsSingle.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="Single120.dat"
datafile2="Single150.dat"
datafile3="Single240.dat"
datafile4="Single60.dat"
datafile5="Single300.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Memory (in KB)" -y "False Positive %" --yrange "[0:0.03]"\
    --bmargin 5.2 --rmargin 5 \
    $datafile5 "k = 300" "1:8" \
    $datafile3 "k = 240" "1:8" \
    $datafile2 "k = 150" "1:8"\
    $datafile4 "k = 60" "1:8" \
    | gnuplot ; epstopdf $outfile --autorotate=All
