# Realtime Autonomous Traffic Signal Switching system 

Objective: Traffic signal switching happens to be one the weakest link in the entire traffic flow system. The current traffic switching system relies on preset timing or a manual controller system based mechanism. We planned to optimise the traffic light switching system using Reinforcement learning in a non-intrusive method.

We thought of adapting the autonomous helicopter training mechanism to model the Traffic Switching system.

We have successfully integrated our q-learning algorithm with realtime traffic flow information provided by HERE maps API. We collected the data for 5 days, and then performed a comparative analysis (plots attached) on how much improvement in traffic flow our model can achieve in terms of avg. waiting time.

A suitable extension was to have large negative weights if important vehicles (ambulance, police, fire cars) gets stopped at signals. The algorithm adapts itself to provide a congestion free path for them as much as possible.

## How to run the code :

#### OS requirement: *nix machine
#### Run this on terminal
#### To run the live demo :

    $ java Main $(echo `node fetch_stats.js`)
    
#### To generate graphs :

    $node script.js 
    $make
    $java Main dailytime.csv 
    $sudo pip install -r requirements.txt 
    $python plotting.py
    
## Visualization

![https://prakhar-agarwal.github.io/traffic-qlearning/gif/ambulance-1-d79d2a29f8.gif](https://prakhar-agarwal.github.io/traffic-qlearning/gif/ambulance-1-d79d2a29f8.gif)

* Values in brackets next to the roads are traffic intensity values fetched via HERE-MAPS api.
* An ambulance is added(location randomly generated - RED/WHITE - MG Road to Kasturba Road) to indicate autonomous Traffic signals switching to provide congestion free path to important vehicles

## Demo Video

<a href="https://youtu.be/iK1LfPHcpXc" title="Title"> Realtime Autonomous Traffic Signal Switching system - Demo </a></p>
