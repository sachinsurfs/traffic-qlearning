import interfaces.Car;
import interfaces.LearningModule;
import interfaces.RoadMap;
import interfaces.TrafficLight;
import utils.Coords;

import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * Class to run simulation with discrete intervals
 */
public class Main {
    public static void main (String[] args) {
        // Inputting arguments, or defaulting to
        // reward 1 and intensity 0.25

        Integer rewardFunction;
        Double trafficIntensity;
        int defaultRewardFunction = 1;
        double defaultTrafficIntensity = 0;
        rewardFunction = defaultRewardFunction;
        trafficIntensity = defaultTrafficIntensity;
        List<String[]> intensityList = new ArrayList<String[]>();
        List<String> APITime = new ArrayList<String>();


        boolean vary = false;
        boolean graphicalOutput = false;
        boolean consoleOutput = false;

        int intensityRange  = 5;
        if (args.length == 1) {
        	vary = true;
    		try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {

    			String sCurrentLine;
    			//Consume first line
    			sCurrentLine = br.readLine();
    			while ((sCurrentLine = br.readLine()) != null) {
    				String [] tmp = sCurrentLine.split(",");
    				String time = tmp[0];
        			String[] currIntensity = Arrays.copyOfRange(tmp, 1, tmp.length);
        			intensityList.add(currIntensity);
        			APITime.add(time);
        		}


    		} catch (IOException e) {
    			graphicalOutput = true;
    			String[] currIntensity = args[0].split(",");
        		intensityList.add(currIntensity);
                System.out.println("Fetched Traffic intensities are " + args[0]);
        		APITime.add("Now");
    		}
        	intensityRange = intensityList.size();
    	}

        //Simulation parameters
        final int TIMESTEP_INTERVAL = 500;
        final int TESTING_INTENSITY_INTERVAL = 500;

        int SIMULATION_TIME = TESTING_INTENSITY_INTERVAL * intensityRange;

        final int STEP_TIME = 100;

        //Graphics and runtime parameters

        boolean output = graphicalOutput || consoleOutput;
        int score = 0;

        if(!output) {
        	System.out.println("Traffic Training and Simulation running... ETA : 10 Minutes");
        }

        int[] training_times = {0,250000};
        for(int time: training_times) {
    	final int TRAINING_TIME = time;
        final int TRAINING_INTENSITY_INTERVAL = TRAINING_TIME/5; //Change 5 to 4 , to skip 1.0 training intensity



        //output parameters
        long iterations = 0;
        long totalCars = 0;
        long totalCarsStopped = 0;
        long tmpCars = 0;
        long totalCarsStopped1 = 0;

        int maxCarsStopped = 0;


        //AVERAGE ITER
        final int BIG_ITER=1;

        //Initialise map, list of cars currently on map, and list of
        //trafficlights
        RoadMap map = new RoadMapImpl();
        List<Car> cars = new ArrayList<Car>();
        List<TrafficLight> trafficLights =
                new ArrayList<TrafficLight>();
        trafficLights.add(
                new TrafficLightImpl(new Coords(20, 20),false));
        trafficLights.add(
                new TrafficLightImpl(new Coords(20, 40),true));
        trafficLights.add(
                new TrafficLightImpl(new Coords(40, 20),true));
        trafficLights.add(
                new TrafficLightImpl(new Coords(40, 40),false));

        //Set actionposition based on arg1
        int actionPosition = 1000;
        switch (rewardFunction) {
            case 1:
                actionPosition = 1000;
                break;
            case 2:
            case 3:
                actionPosition = 100000;
                break;
            }
        Viewer v = graphicalOutput ? new Viewer() : null;

        
        //Simulation tests
        int currentTimeStep;


        //Basic logic for each time step
        // - change traffic lights if required - call a function from
        //   'learning' class to do this
        // - move cars in their current direction by velocity (modify
        //   velocity if necessary - using CarAI)
        // - spawn cars at extremities
        // - Now that we have the new state, update the qvalue for the
        //  previous s,a pair

        //TRAINING TIME

        File varTmpDir = new File("./20k");
        boolean exists = varTmpDir.exists();
        LearningModule learningModule;
        int timeRan = 0;

        if (exists && TRAINING_TIME!=0) {
        	try {
        		FileInputStream fi = new FileInputStream(new File("./20k"));
    			ObjectInputStream oi = new ObjectInputStream(fi);


    			learningModule =  (LearningModule) oi.readObject();

    			oi.close();
    			fi.close();
        	}
         catch (Exception e) {
 			learningModule = new LearningModuleImpl(actionPosition);
			e.printStackTrace();
			System.exit(-1); //Can be removed for skipping training.
		}

        }

        else {
        learningModule = new LearningModuleImpl(actionPosition);


        for (timeRan = 0; timeRan < TRAINING_TIME; timeRan++) {

            if(timeRan % TRAINING_INTENSITY_INTERVAL ==0) {
              trafficIntensity +=0.2;
              //System.out.println(trafficIntensity);
            }
            //Params required to learn
            RoadMap currentState = map.copyMap();
            currentState.addCars(cars);
            List<Boolean> switchedLights;
            List<Integer> states = new ArrayList<Integer>();
            List<Integer> nextStates = new ArrayList<Integer>();
            List<Integer> rewards = new ArrayList<Integer>();

            //Save the states of each traffic light before updating
            for (TrafficLight light: trafficLights) {
                switch (rewardFunction) {
                    case 1:
                        states.add(currentState.stateCode(light));
                        break;
                    case 2:
                        states.add(currentState.stateCode2(light));
                        break;
                    case 3:
                        states.add(currentState.stateCode3(light, cars));
                        break;
                }
            }

            //Use the learned values to update the traffic lights
            switchedLights = learningModule.updateTrafficLights(
                    currentState, trafficLights, timeRan
            );

            //copy updated state of map
            RoadMap nextState = currentState.copyMap();

            //Move cars currently on map
            List<Car> carsToRemove = new ArrayList<Car>();
            for (Car car : cars) {
                car.move(
                        currentState.getClosestTrafficLight(
                                car, trafficLights),
                        nextState);
                int x=car.getCoords().getX(), y=car.getCoords().getY();
                if (x<0 || x>=60 || y<0 || y>=60) {
                     carsToRemove.add(car);
                }
            }
            cars.removeAll(carsToRemove);

            //Spawn cars onto map extremities
            for (Coords roadEntrance : map.getRoadEntrances()) {
                if (
                    Math.random() <= trafficIntensity &&
                    !currentState.carAt(roadEntrance)
                ) {
                    Car c = new CarImpl(
                            new Coords(roadEntrance),
                            map.getStartingVelocity(roadEntrance)
                    );
                    cars.add(c);
                    totalCars ++;
                }
            }
            nextState.addCars(cars);

            //Update statistics
            iterations++;
            int localCarsStopped = 0;
            for (Car car : cars) {
                int dx = car.getVelocity().getXSpeed();
                int dy = car.getVelocity().getYSpeed();
                if (dx == 0 && dy == 0) {
                    localCarsStopped++;
                }
            }
            totalCarsStopped += localCarsStopped;

            if (localCarsStopped > maxCarsStopped) {
                maxCarsStopped = localCarsStopped;
            }

            // Updates q-values
            //calculate reward and state code for each traffic light
            for (TrafficLight light : trafficLights) {
                switch (rewardFunction) {
                    case 1:
                        rewards.add(learningModule.reward(
                                nextState.stateCode(light)));
                        nextStates.add(nextState.stateCode(light));
                        break;
                    case 2:
                        rewards.add(learningModule.reward2(
                                nextState.stateCode2(light)));
                        nextStates.add(nextState.stateCode2(light));
                        break;
                    case 3:
                        rewards.add(learningModule.reward3(
                                nextState.stateCode3(light, cars)));
                        nextStates.add(nextState.stateCode3(light, cars));
                        break;
                }

            }


            learningModule.learn(
                states, switchedLights, rewards, nextStates,
                trafficLights
            );
          }


        //Object saving

        try {
        	if(TRAINING_TIME!=0) {
			FileOutputStream f = new FileOutputStream(new File("./20k"));
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write objects to file
			o.writeObject(learningModule);

			o.close();
			f.close();
        	}

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
        }

        //KEY = Intensity;Timestep  VALUE = sum of avg wait-times
        HashMap<String, Float> hmap = new HashMap<String, Float>();


        for(int iter=0;iter<BIG_ITER;iter++) {
          trafficIntensity =0.0;
          cars.clear();

          //Simulation time

          int index = 0;
          String[] currIntensity = {"","","","","","","",""};
          String currTime = "";
          for (timeRan = 0; timeRan < SIMULATION_TIME; timeRan++) {
        	  if(timeRan % TESTING_INTENSITY_INTERVAL ==0) {
        		  if(vary) {
        			  currIntensity = intensityList.get(index);
        			  currTime = APITime.get(index);
        			  index+=1;
        		  }
        		  else {
                      trafficIntensity +=0.2;
                      cars.clear();
        		  }
        		  totalCarsStopped1 += totalCarsStopped;
        		  tmpCars = totalCarsStopped;
                  totalCarsStopped = 0;
                  //Clearing all cars after  every intensity interval

                }

              //Params required to learn
              RoadMap currentState = map.copyMap();
              currentState.addCars(cars);
              List<Boolean> switchedLights;
              List<Integer> states = new ArrayList<Integer>();
              List<Integer> nextStates = new ArrayList<Integer>();
              List<Integer> rewards = new ArrayList<Integer>();

              //Save the states of each traffic light before updating
              for (TrafficLight light: trafficLights) {
                  switch (rewardFunction) {
                      case 1:
                          states.add(currentState.stateCode(light));
                          break;
                      case 2:
                          states.add(currentState.stateCode2(light));
                          break;
                      case 3:
                          states.add(currentState.stateCode3(light, cars));
                          break;
                  }
              }

              //Use the learned values to update the traffic lights
              switchedLights = learningModule.updateTrafficLights(
                      currentState, trafficLights, timeRan
              );

              //copy updated state of map
              RoadMap nextState = currentState.copyMap();

              //Move cars currently on map
              List<Car> carsToRemove = new ArrayList<Car>();
              for (Car car : cars) {
                  car.move(
                          currentState.getClosestTrafficLight(
                                  car, trafficLights),
                          nextState);
                  int x=car.getCoords().getX(), y=car.getCoords().getY();
                  if (x<0 || x>=60 || y<0 || y>=60) {
                       carsToRemove.add(car);
                  }
              }
              cars.removeAll(carsToRemove);

              int i_tmp=0;
              //Spawn cars onto map extremities
              for (Coords roadEntrance : map.getRoadEntrances()) {

            	  if(vary) {
            		  trafficIntensity = Double.valueOf(currIntensity[i_tmp])/10;
	            	  i_tmp+=1;
            	  }
            	  if (
                      Math.random() <= trafficIntensity &&
                      !currentState.carAt(roadEntrance)
                  ) {
                      Car c = new CarImpl(
                              new Coords(roadEntrance),
                              map.getStartingVelocity(roadEntrance)
                      );
                      cars.add(c);
                      totalCars ++;
                  }
              }
              nextState.addCars(cars);

              //Update statistics
              iterations++;
              int localCarsStopped = 0;
              for (Car car : cars) {
                  int dx = car.getVelocity().getXSpeed();
                  int dy = car.getVelocity().getYSpeed();
                  if (dx == 0 && dy == 0) {
                      localCarsStopped++;
                  }
              }
              totalCarsStopped += localCarsStopped;


              if (localCarsStopped > maxCarsStopped) {
                  maxCarsStopped = localCarsStopped;
              }


              if (graphicalOutput) {
                  v.view(map, cars, trafficLights, currIntensity);
                 
                  
              }
              if (consoleOutput) {
                  map.print(cars, trafficLights);
              }
              if (output) {
                  try {
                      Thread.sleep(STEP_TIME);
                  } catch (Exception ignored) {}
              }
              for (Car c : cars) {
              int dx = c.getVelocity().getXSpeed();
              int dy = c.getVelocity().getYSpeed();
                  score += dx==0&&dy==0 ? -1 : 0;
              }


              if(TRAINING_TIME !=0) {
              // Updates q-values
              //calculate reward and state code for each traffic light
              for (TrafficLight light : trafficLights) {
                  switch (rewardFunction) {
                      case 1:
                          rewards.add(learningModule.reward(
                                  nextState.stateCode(light)));
                          nextStates.add(nextState.stateCode(light));
                          break;
                      case 2:
                          rewards.add(learningModule.reward2(
                                  nextState.stateCode2(light)));
                          nextStates.add(nextState.stateCode2(light));
                          break;
                      case 3:
                          rewards.add(learningModule.reward3(
                                  nextState.stateCode3(light, cars)));
                          nextStates.add(nextState.stateCode3(light, cars));
                          break;
                  }

              }


              learningModule.learn(
                  states, switchedLights, rewards, nextStates,
                  trafficLights
              );
              }

         if (timeRan % TIMESTEP_INTERVAL ==0) {
        	  Integer tmp = timeRan% TESTING_INTENSITY_INTERVAL;
        	  String key;
        	  if(vary) {
        		  //index instead of traffic intensity
            	  key = currTime+";"+tmp.toString();
        	  }
        	  else {
        		  key = trafficIntensity.toString()+";"+tmp.toString();
        	  }
        	hmap.put(key , hmap.getOrDefault(key, (float)0.0) + ((float)tmpCars)/TESTING_INTENSITY_INTERVAL);

            //System.out.println(trafficIntensity+","+((float)totalCarsStopped)/TESTING_INTENSITY_INTERVAL +","+ timeRan% TESTING_INTENSITY_INTERVAL );
          }


          }


        }


        try{
            PrintWriter writer;
            if(TRAINING_TIME==0) {
            	writer = new PrintWriter("without_training.csv", "UTF-8");
            }
            else {
            	writer = new PrintWriter("with_training.csv", "UTF-8");
            }

            for (Map.Entry<String, Float> entry : hmap.entrySet()) {
                String key[] = entry.getKey().split(";");

                String trafficIndex;
                trafficIndex = key[0];
                int currTimeStep = Integer.valueOf(key[1]);
                Float waitTime = entry.getValue()/BIG_ITER;
                if(vary) {
                    writer.println(trafficIndex+","+waitTime);
                }
                else {
                	writer.println(trafficIndex+","+waitTime+","+ currTimeStep);

                }
            }
            writer.close();
            System.out.println("Output saved to file!");
        } catch (IOException e) {
           System.out.println("Output could not be written to file\nConsole Output:");
           for (Map.Entry<String, Float> entry : hmap.entrySet()) {
               String key[] = entry.getKey().split(";");

               String trafficIndex;
               trafficIndex = key[0];
               int currTimeStep = Integer.valueOf(key[1]);
               Float waitTime = entry.getValue()/BIG_ITER;
               if(vary) {

               	System.out.println(trafficIndex+","+waitTime );
               }
               else {
               	System.out.println(trafficIndex+","+waitTime+","+ currTimeStep );

               }
           }


        }
        }

    }

}
