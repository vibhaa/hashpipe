plotscript="pubplot.py"
outfile="Duplicates.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="SingleD2.dat"
datafile2="SingleD4.dat"
datafile3="SingleD8.dat"
datafile4="Single60FP.dat"
datafile5="Single300FP.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Memory (in KB)" -y "Duplicate Entries %" --yrange "[0:7.5]"\
    --bmargin 5.2 --rmargin 5 \
     -k "top left vertical width 20 spacing 2.0 font \"Helvetica,24\"" \
     $datafile3 "d=8" "1:10" \
     $datafile2 "d=4" "1:10" \
    $datafile1 "d=2" "1:10" \
    | gnuplot ; epstopdf $outfile --autorotate=All
