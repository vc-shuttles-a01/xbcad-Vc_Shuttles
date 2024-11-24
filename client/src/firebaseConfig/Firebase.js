import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyC3dCEu8Y2qRE22ve0fq88JkEW97b_q3h4",
  authDomain: "shuttleapp-75184.firebaseapp.com",
  projectId: "shuttleapp-75184",
  storageBucket: "shuttleapp-75184.firebasestorage.app",
  messagingSenderId: "386782838163",
  appId: "1:386782838163:web:67454b6d44d972f5953c66",
  measurementId: "G-13KNP4214Q"
};
const app = initializeApp(firebaseConfig);

// Initialize Firebase services
const db = getFirestore(app);
const auth = getAuth(app);

export { db, auth };
