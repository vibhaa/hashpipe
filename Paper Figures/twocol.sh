plotscript="pubplot.py"
outfile="output1_file.eps" # will be later converted to PDF by epstopdf
datapath="folder_containing_data_files/"
datafilename="dummy.dat"
prefix="chs-atl-2012.09.17-00:00:00-24h-utilization-counter-aggregate-all-per_event"
max_time=5

python $plotscript -l --small -g -a "font \"Helvetica,34\"" \
    -p 2 -f "postscript enhanced color" \
    -o $outfile \
    -x "Time (fraction of max)" \
    -y "CDF (time since last TE) " --yrange "[0:1]" \
    --bmargin 8.5 --rmargin 5 --lmargin 17 \
    -k "top left vertical width 20 spacing 2.0 font \"Helvetica,34\"" \
    $datafilename "loss >= 5%" "(\$1 / $max_time ):2" \
    | gnuplot ; epstopdf $outfile --autorotate=All
