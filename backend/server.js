const express = require('express');
const cors = require('cors');
const firebaseAdmin = require('firebase-admin');
require('dotenv').config();
const fs = require('fs');
const https = require('https');


// Provide the correct path to your service account JSON file
const serviceAccount = require("./shuttleapp-75184-8f0d1d64a8e3.json");

firebaseAdmin.initializeApp({
  credential: firebaseAdmin.credential.cert(serviceAccount),
  databaseURL: "https://shuttleapp-75184.firebaseio.com"  // Ensure this URL matches your Firebase Realtime Database URL
});

console.log("Firebase Admin initialized");

const app = express();
app.use(cors({ origin: 'https://localhost:3000', credentials: true })); // Allow requests from frontend origin
app.use(express.json());

// Sample API route
app.get('/', (req, res) => {
  res.send('Backend API is working');
});

// Firebase authentication route example
app.post('/api/login', async (req, res) => {
  try {
    const { token } = req.body;  // Assume token is sent from frontend
    const decodedToken = await firebaseAdmin.auth().verifyIdToken(token);
    res.status(200).send(decodedToken);
  } catch (error) {
    res.status(400).send('Error verifying token');
  }
});

const options = {
  key: fs.readFileSync('./keys/privatekey.pem'),
  cert: fs.readFileSync('./Keys/certificate.pem')
}

// Start HTTPS server
const PORT = process.env.PORT || 5001;
https.createServer(options, app).listen(PORT, () => {
    console.log(`HTTPS Server running on https://localhost:${PORT}`);
});
