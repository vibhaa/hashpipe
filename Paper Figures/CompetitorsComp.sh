plotscript="pubplot.py"
outfile="CompFalseNeg.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="Single150.dat"
datafile2="Baseline150.dat"
datafile3="SH150.dat"
datafile4="SS150.dat"
datafile5="CM150.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Memory (in KB)" -y "False Negative %" --yrange "[0:50]"\
    --bmargin 5.2 --rmargin 4.3 \
    $datafile3 "Sample and Hold" "1:4" \
    $datafile5 "Count-Min + Cache" "1:4"\
    $datafile1 "HashPipe" "1:4" \
    | gnuplot ; epstopdf $outfile --autorotate=All
