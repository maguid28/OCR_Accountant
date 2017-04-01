#!/bin/bash


tesseract eng.rec.exp0.png eng.rec.exp0 box.train
tesseract eng.rec.exp1.png eng.rec.exp1 box.train
tesseract eng.rec.exp2.png eng.rec.exp2 box.train
tesseract eng.rec.exp3.png eng.rec.exp3 box.train
tesseract eng.rec.exp4.png eng.rec.exp4 box.train
tesseract eng.rec.exp5.png eng.rec.exp5 box.train
tesseract eng.rec.exp6.png eng.rec.exp6 box.train
tesseract eng.rec.exp7.png eng.rec.exp7 box.train
tesseract eng.rec.exp8.png eng.rec.exp8 box.train
tesseract eng.rec.exp9.png eng.rec.exp9 box.train
tesseract eng.rec.exp10.png eng.rec.exp10 box.train
tesseract eng.rec.exp11.png eng.rec.exp11 box.train
tesseract eng.rec.exp12.png eng.rec.exp12 box.train
tesseract eng.rec.exp13.png eng.rec.exp13 box.train
tesseract eng.rec.exp14.png eng.rec.exp14 box.train
tesseract eng.rec.exp15.png eng.rec.exp15 box.train
tesseract eng.rec.exp16.png eng.rec.exp16 box.train

unicharset_extractor eng.rec.exp0.box eng.rec.exp1.box eng.rec.exp2.box eng.rec.exp3.box eng.rec.exp4.box eng.rec.exp5.box eng.rec.exp6.box eng.rec.exp7.box eng.rec.exp8.box eng.rec.exp9.box eng.rec.exp10.box eng.rec.exp11.box eng.rec.exp12.box eng.rec.exp13.box eng.rec.exp14.box eng.rec.exp15.box eng.rec.exp16.box

set_unicharset_properties --F eng.font_properties -U unicharset -O output_unicharset --script_dir=ucs

shapeclustering -F eng.font_properties -U output_unicharset  eng.rec.exp0.tr eng.rec.exp1.tr eng.rec.exp2.tr eng.rec.exp3.tr eng.rec.exp4.tr eng.rec.exp5.tr eng.rec.exp6.tr eng.rec.exp7.tr eng.rec.exp8.tr eng.rec.exp9.tr eng.rec.exp10.tr eng.rec.exp11.tr eng.rec.exp12.tr eng.rec.exp13.tr eng.rec.exp14.tr eng.rec.exp15.tr eng.rec.exp16.tr

mftraining -F eng.font_properties -U output_unicharset  eng.rec.exp0.tr eng.rec.exp1.tr eng.rec.exp2.tr eng.rec.exp3.tr eng.rec.exp4.tr eng.rec.exp5.tr eng.rec.exp6.tr eng.rec.exp7.tr eng.rec.exp8.tr eng.rec.exp9.tr eng.rec.exp10.tr eng.rec.exp11.tr eng.rec.exp12.tr eng.rec.exp13.tr eng.rec.exp14.tr eng.rec.exp15.tr eng.rec.exp16.tr

cntraining -F eng.font_properties -U output_unicharset  eng.rec.exp0.tr eng.rec.exp1.tr eng.rec.exp2.tr eng.rec.exp3.tr eng.rec.exp4.tr eng.rec.exp5.tr eng.rec.exp6.tr eng.rec.exp7.tr eng.rec.exp8.tr eng.rec.exp9.tr eng.rec.exp10.tr eng.rec.exp11.tr eng.rec.exp12.tr eng.rec.exp13.tr eng.rec.exp14.tr eng.rec.exp15.tr eng.rec.exp16.tr
