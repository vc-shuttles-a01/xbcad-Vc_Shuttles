import React, { useState, useEffect } from "react";

import { useNavigate } from "react-router-dom";
import "./OpeningPage.css";

import book from "../Assets/Book.png";
import mytrip from "../Assets/MyTrips.png";
import schedule from "../Assets/Schedule.png";
import track  from "../Assets/Track.png";
import toandfrom from "../Assets/ToAndFRom.png";
import fromandto from "../Assets/Fromandto.png";
import logo from "../Assets/shuttle_logo.png";
//import { setLogFunction } from "firebase-admin/firestore";


const slides = [
  {
    title: "Welcome to VC Shuttles",
    text: "Efficient and reliable transportation for you.",
    bgColor: "#f5ba65",
    image: logo, // Add image path or URL
  },
  
  {
      title: "Booking a seat made easy",
      text: "Efficient and reliable transportation for you.",
      bgColor: "#f9e592",
      image: book, // Add image path or URL
    },
    {
      title: "With a easy to understand schedule",
      text: "making it easy to plan ahead.",
      bgColor: "#6f4e37",
      image: schedule,
    },
    {
      title: "Making it easy for users to get there bookings",
      text: "our QR ticket service makes it easy",
      bgColor: "#d8c993",
      image: mytrip,
    },
    {
      title: "Safe & Secure",
      text: "Your safety is our top priority, You can track the shuttle where ever it is.",
      bgColor: "#3d2b1f",
      image: track,
    },
    {
      title: "With dedicated Routes to and from campus",
      text: "Connecting you to your destinations with ease.",
      bgColor: "#e9ad3c",
      image: toandfrom,
      //image:"client\\src\\Assets\\Screenshot 2024-11-24 020303.png",
    },
    {
      title: "We ensure user safety from the gautrain station ",
      text: "Connecting you to your destinations with the peace of mind YOU deserve.",
      bgColor: "#6c541e",
      image: fromandto, // If no image, leave empty
    },
  
  ];

const OpeningPage = () => {
  const [currentSlide, setCurrentSlide] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentSlide((prevSlide) => (prevSlide + 1) % slides.length);
    }, 10000); // Change slide every 3 seconds
    return () => clearInterval(interval);
  }, []);

  const reg = () =>{
    navigate("/Register");
    
  }
  const log = () =>{
    navigate("/Login");
  }

  return (
    <div className="opening-page">
      <header className="opening-header">
        <h1>VC Shuttles</h1>
        <div className="header-buttons">
          <button className="btn" onClick={reg}>Signup</button>
          <button className="btn" onClick={log}>Login</button>
        </div>
      </header>

      <div className="carousel">
        {slides.map((slide, index) => (
          <div
            key={index}
            className={`slide ${currentSlide === index ? "active" : ""}`}
            style={{ backgroundColor: slide.bgColor }}
          >
            <h2>{slide.title}</h2>
            {slide.image && <img src={slide.image} alt={slide.title} className="slide-image" />}
        <br/>
            <p>{slide.text}</p>
           

          </div>
        ))}
      </div>

      <div className="text-panel">
        <div className="text-block">Convenient Shuttle Services</div>
        <div className="text-block">Never get left behind</div>
        <div className="text-block"> Book Your Ride Online</div>
        <div className="text-block">Available on the Google Play Store</div>
      </div>

      <footer className="footer">
        <p>© VC Shuttles Sandton</p>
      </footer>
    </div>
  );
};

export default OpeningPage;
