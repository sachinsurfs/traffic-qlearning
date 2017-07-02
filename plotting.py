#!/usr/bin/python
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from bokeh.plotting import figure, output_file, show
import datetime
from dateutil import parser
qlearning_time = pd.read_csv("with_training.csv",header=None)
timeTriggered_time = pd.read_csv("without_training.csv",header=None)
qlearning_time.columns = ["time","val1"]
timeTriggered_time.columns = ["time","val1"]
timeTriggered_time['time'] = pd.to_datetime(timeTriggered_time.time, format='%H:%M:%S').dt.time
qlearning_time['time'] = pd.to_datetime(qlearning_time.time, format='%H:%M:%S').dt.time
timeTriggered_time = timeTriggered_time.sort_values(by='time',ascending=True)
qlearning_time = qlearning_time.sort_values(by='time',ascending=True)
Y1 = np.array(qlearning_time['val1'], dtype=np.float32)
Y2 = np.array(timeTriggered_time['val1'], dtype=np.float32)
output_file("line.html")
p = figure(plot_width=1300, plot_height=700,x_axis_type='datetime')
p.xaxis[0].axis_label = 'Time of the day (Starts at 00:00 am)'
p.yaxis[0].axis_label = 'Avg. wait time (Timesteps)'
p.line(qlearning_time['time'], Y1, line_dash=(4, 4),line_width=2,legend="With Q-learning",line_color = "orange" )
p.line(timeTriggered_time['time'], Y2, line_width=2,legend="Without training",line_color = "blue")
show(p)
