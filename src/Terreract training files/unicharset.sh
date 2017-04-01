#!/bin/bash

unicharset_extractor eng.rec.exp0.box eng.rec.exp1.box eng.rec.exp2.box eng.rec.exp3.box eng.rec.exp4.box eng.rec.exp5.box eng.rec.exp6.box eng.rec.exp7.box eng.rec.exp8.box eng.rec.exp9.box eng.rec.exp10.box eng.rec.exp11.box eng.rec.exp12.box eng.rec.exp13.box eng.rec.exp14.box eng.rec.exp15.box eng.rec.exp16.box

set_unicharset_properties --F eng.font_properties -U unicharset -O output_unicharset --script_dir=ucs
