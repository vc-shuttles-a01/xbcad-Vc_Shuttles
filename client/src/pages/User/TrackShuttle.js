import React, { useEffect, useState } from "react";
import {  useNavigate } from "react-router-dom";

const TrackShuttle = () => {
  const [route, setRoute] = useState("RouteA");
  const [shuttleLocation, setShuttleLocation] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (shuttleLocation) {
      const map = new window.google.maps.Map(document.getElementById("map"), {
        zoom: 12,
        center: shuttleLocation,
      });

      new window.google.maps.Marker({
        position: shuttleLocation,
        map: map,
        title: "Current Shuttle Location",
      });
    }
  }, [shuttleLocation]);

  const trackShuttle = () => {
    fetch(`/api/track-shuttle?route=${route}`)
      .then((response) => response.json())
      .then((data) => {
        if (data.latitude && data.longitude) {
          setShuttleLocation({
            lat: data.latitude,
            lng: data.longitude,
          });
        } else {
          alert("No current location data available for the selected route.");
        }
      });
  };

  return (
    <div className="container">
      <h2>Track Shuttle</h2>
      <button className="btn btn-back" onClick={navigate('./StudentDashboard')}>back </button>
      <div className="form-group">
        <label htmlFor="route">Select Route:</label>
        <select
          id="route"
          className="form-control"
          value={route}
          onChange={(e) => setRoute(e.target.value)}
        >
          <option value="RouteA">Route A</option>
          <option value="RouteB">Route B</option>
          <option value="RouteC">Route C</option>
        </select>
      </div>
      <div className="form-group">
        <button className="btn-track" onClick={trackShuttle}>
          Track Shuttle
        </button>
      </div>
      <div id="map" style={{ width: "100%", height: "400px" }}></div>
    </div>
  );
}

export default TrackShuttle;
