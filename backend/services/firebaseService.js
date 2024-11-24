// Import the required Firebase Admin SDK modules
const admin = require('firebase-admin');
const serviceAccount = require('.C:\Users\bungy\VC_Shuttles\backend\shuttleapp-75184-firebase-adminsdk-qoudn-f87c2b4489.json');  // Path to your Firebase Admin SDK private key

// Initialize Firebase Admin SDK with the service account
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://console.firebase.google.com/project/shuttleapp-75184/firestore"
  // Replace with your Firebase Realtime Database URL if using Realtime Database
});

// Firebase Authentication functions
const verifyIdToken = async (idToken) => {
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    return decodedToken;  // Returns the decoded token with user information
  } catch (error) {
    throw new Error('Authentication failed');
  }
};

// Firestore functions (if you are using Firestore)
const getUserData = async (uid) => {
  try {
    const userRef = admin.firestore().collection('users').doc(uid);
    const doc = await userRef.get();
    if (!doc.exists) {
      throw new Error('No user found');
    }
    return doc.data();  // Return user data from Firestore
  } catch (error) {
    throw new Error('Error getting user data: ' + error.message);
  }
};