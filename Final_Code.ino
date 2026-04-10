// Pin Definitions : PWM Pins are : 3, 5, 6, 9, 10 ,11
// LED 1
const int redPin1 = 9; 
const int greenPin1 = 10; 
const int bluePin1 = 11;
// LED 2
const int redPin2 = 3; 
const int greenPin2 = 5; 
const int bluePin2 = 6;
// LED 3
const int redPin3 = 4; 
const int greenPin3 = 7; 
const int bluePin3 = 8;

// Variables
int redValue_led3 = 0;
int greenValue_led3 = 0;  
int blueValue_led3 = 0; 
byte receivedByte = 0 ;
// each 1 Byte , to store the received byte from the Bluetooth module
byte packet[15] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

byte ByteCounter = 0, LED = 0;


// Setup function :Setup Function: The setup function is called once at the start of the program. 
//It initializes the pins connected to the LEDs as output pins and starts the serial communication at a baud rate of 9600 (the number of changes to the signal per second)
void setup() {
// Initialize the RGB LED pins as OUTPUT 
 pinMode(redPin1, OUTPUT);
 pinMode(greenPin1, OUTPUT); 
 pinMode(bluePin1, OUTPUT); 
 pinMode(redPin2, OUTPUT); 
 pinMode(greenPin2, OUTPUT);
 pinMode(bluePin2, OUTPUT); 
 pinMode(redPin3, OUTPUT); 
 pinMode(greenPin3, OUTPUT); 
 pinMode(bluePin3, OUTPUT);


 Serial.begin(9600);   //Serial UART Connection  , Defauld baud rate of HC-06 Bluetooth module is 9600
 
}

// Loop function 
void loop() {

if (Serial.available() > 0) {
// Check if there is data available to read 

 receivedByte = Serial.read();

 if (ByteCounter == 0) {
   if (receivedByte == 'L') {
     packet[(ByteCounter + (5 * LED))] = receivedByte; 
     ByteCounter++;
}

} else if ( ByteCounter == 1 ) { 
   if (receivedByte == '1') { 
     packet[1] = receivedByte; 
     ByteCounter = 2;
     LED = 0;

} else if (receivedByte == '2') { 
   packet[6] = receivedByte; 
   ByteCounter = 2;
   LED = 1;

} else if (receivedByte == '3') { 
   packet[11] = receivedByte; 
   ByteCounter = 2;
   LED = 2;
}

} else {     // if ByteCounter is not 0 and is not 1 (can be 2,3 or 4) // this part is for R , G , B s
   packet[ByteCounter + (LED * 5)] = receivedByte; 
   ByteCounter++;

if (ByteCounter + (LED * 5) > 14) { 
   ByteCounter = 0;
   LED = 0;
}
if (ByteCounter >4){
  ByteCounter = 0;
  LED++;
}
}

}
}
//analogWrite() Writes an analog value (PWM wave) to a pin, used to light LEDs
//After a call to analogWrite(), the pin will generate a steady rectangular wave of the specified duty cycle
analogWrite(redPin1, packet[2]); 
analogWrite(greenPin1, packet[3]); 
analogWrite(bluePin1, packet[4]); 
analogWrite(redPin2, packet[7]); 
analogWrite(greenPin2, packet[8] ); 
analogWrite(bluePin2, packet[9] );

redValue_led3 = (packet[12]); 
greenValue_led3 = (packet[13]); 
blueValue_led3 = (packet[14]);

// a loop for making a software based PWM for LED 3

for ( int k = 0 ; k <= 255 ; k++) {  // defining duty cycle

  if (redValue_led3 >= k) {
   digitalWrite(redPin3, HIGH);
  } else {
   digitalWrite(redPin3, LOW);
  }

  if (greenValue_led3 >= k) {
   digitalWrite(greenPin3, HIGH);
  } else {
   digitalWrite(greenPin3, LOW);
  }
  
  if (blueValue_led3 >= k) {
   digitalWrite(bluePin3, HIGH);
  } else {
   digitalWrite(bluePin3, LOW);
  }
//delayMicroseconds(1);

}



