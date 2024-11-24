import React, { useEffect, useState } from 'react';
import { db } from '../../firebaseConfig/Firebase';
import { collection, query, where, getDocs } from 'firebase/firestore';
import './AdminReport.css';
import Layout from '../AdminLayout';
//import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const AdminReports = () => {
  const [reports, setReports] = useState([]);

  useEffect(() => {
    fetchRidershipData();
    fetchPeakHoursData();
    fetchRouteEfficiencyData();
  }, []);

  // Method to map and format the fetched data
  const mapFetchedData = (data) => {
    return {
      totalRides: data.totalRides,
      peakHour: data.peakHour,
      avgDistance: data.avgDistance,
      avgTime: data.avgTime,
    };
  };

  const fetchRidershipData = async () => {
    const todayDate = new Date();
    const lastWeekDate = new Date();
    lastWeekDate.setDate(todayDate.getDate() - 7);

    const formattedToday = todayDate.toLocaleDateString('en-GB').replace(/\//g, '-');
    const formattedLastWeek = lastWeekDate.toLocaleDateString('en-GB').replace(/\//g, '-');

    try {
      const schedulesQuery = query(
        collection(db, 'schedules'),
        where('date', '>=', formattedLastWeek),
        where('date', '<=', formattedToday)
      );
      const querySnapshot = await getDocs(schedulesQuery);

      let totalRides = 0;
      querySnapshot.forEach((doc) => {
        totalRides += doc.data().totalSeatsBooked || 0;
      });

      // Call the map method to structure the data
      const mappedData = mapFetchedData({
        totalRides,
      });

      setReports((prev) => [...prev, { title: 'Total Rides (Last Week)', value: mappedData.totalRides }]);
    } catch (error) {
      console.error('Error fetching ridership data:', error);
    }
  };

  const fetchPeakHoursData = async () => {
    try {
      const schedulesQuery = collection(db, 'schedules');
      const querySnapshot = await getDocs(schedulesQuery);

      const hoursMap = {};
      querySnapshot.forEach((doc) => {
        const time = doc.data().time || 'Unknown';
        hoursMap[time] = (hoursMap[time] || 0) + 1;
      });

      const peakHour = Object.entries(hoursMap).reduce(
        (a, b) => (b[1] > a[1] ? b : a),
        ['', 0]
      );

      // Call the map method to structure the data
      const mappedData = mapFetchedData({
        peakHour: peakHour[0],
      });

      setReports((prev) => [...prev, { title: 'Peak Hour', value: mappedData.peakHour }]);
    } catch (error) {
      console.error('Error fetching peak hours data:', error);
    }
  };

  const fetchRouteEfficiencyData = async () => {
    try {
      const routesQuery = collection(db, 'routes');
      const querySnapshot = await getDocs(routesQuery);

      let totalDistance = 0;
      let totalTime = 0;
      let routeCount = 0;

      querySnapshot.forEach((doc) => {
        totalDistance += doc.data().distance || 0;
        totalTime += doc.data().time || 0;
        routeCount++;
      });

      const avgDistance = routeCount ? (totalDistance / routeCount).toFixed(2) : 0;
      const avgTime = routeCount ? (totalTime / routeCount).toFixed(2) : 0;

      // Call the map method to structure the data
      const mappedData = mapFetchedData({
        avgDistance: `${avgDistance} km`,
        avgTime: `${avgTime} mins`,
      });

      setReports((prev) => [
        ...prev,
        { title: 'Avg Distance Per Route', value: mappedData.avgDistance },
        { title: 'Avg Time Per Route', value: mappedData.avgTime },
      ]);
    } catch (error) {
      console.error('Error fetching route efficiency data:', error);
    }
  };

  return (
    <Layout title={"Reports"}>
      <div className="admin-reports-container">
        <div className="reports-list">
          {reports.map((report, index) => (
            <div key={index} className="report-item">
              <h3>{report.title}</h3>
              <p>{report.value}</p>
            </div>
          ))}
        </div>
        
      </div>
    </Layout>
  );
};

export default AdminReports;
