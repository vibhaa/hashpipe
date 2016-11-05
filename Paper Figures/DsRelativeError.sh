plotscript="pubplot.py"
outfile="DsRelativeError.eps" # will be later converted to PDF by epstopdf
datapath="/home/326/paper"
datafile1="HPSizeDist150.dat"
datafile2="HPSizeDistD4.dat"
datafile3="HPSizeDistD2.dat"
datafile4="HPSizeDistD8.dat"
datafile5="CMSizeDist150.dat"
plotpath="/home/326/paper"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
python $plotscript -l -a "font \"Helvetica,24\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Actual Size of Flow (x)" -y "Average estimation error in \nflows of size > x" --yrange "[0:0.6]"\
    --bmargin 5.2 --rmargin 4.8 \
    $datafile3 "d = 2" "1:4"\
    $datafile2 "d = 4" "1:4" \
    $datafile4 "d = 8" "1:4" \
    | gnuplot ; epstopdf $outfile --autorotate=All
