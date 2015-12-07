bool last_read = false;

void setup() {
  Serial.begin(9600);
  pinMode(12, INPUT);
}

void loop() {
  bool pressed = (digitalRead(12) == LOW);
  if(pressed && !last_read) {
    Serial.print("A");
    delay(50);
  }

  if(!pressed && last_read) {
    delay(50);
  }

  last_read = pressed;
}
