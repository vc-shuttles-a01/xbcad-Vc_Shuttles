import React, { useState, useEffect, useCallback } from 'react';
import { getFirestore, collection, getDocs } from 'firebase/firestore';

import Layout from '../Layout';
import './ViewShuttle.css';

function ViewShuttleSchedules() {
  const [schedules, setSchedules] = useState([]);
  const [filterDate, setFilterDate] = useState('');
  const [sortOrder, setSortOrder] = useState('DESC');
  //const navigate = useNavigate();
  const db = getFirestore();

  // Function to fetch schedules from Firestore
  const fetchSchedules = useCallback(async () => {
    try {
      const schedulesSnapshot = await getDocs(collection(db, 'schedules'));
      const fetchedSchedules = schedulesSnapshot.docs.map(docSnapshot => ({
        id: docSnapshot.id,
        ...docSnapshot.data(),
      }));

      console.log('Fetched Schedules:', fetchedSchedules); // Log to check data structure

      // Sorting the schedules
      const sortedSchedules = fetchedSchedules.sort((a, b) =>
        sortOrder === 'ASC'
          ? new Date(a.Date) - new Date(b.Date)
          : new Date(b.Date) - new Date(a.Date)
      );

      // Filtering by date if a filterDate is selected
      const filteredSchedules = filterDate
        ? sortedSchedules.filter(schedule => schedule.Date === filterDate)
        : sortedSchedules;

      // Setting schedules state with the filtered and sorted data
      setSchedules(filteredSchedules);
    } catch (error) {
      console.error('Error fetching schedules', error);
    }
  }, [filterDate, sortOrder, db]);

  useEffect(() => {
    fetchSchedules();
  }, [fetchSchedules]);

  return (
    <Layout title={"schedules"}>
    <div className="container1">
      <h2>View Shuttle Schedules</h2>
      <div className="form-group5">
        <label htmlFor="filterDate">Filter by Date:</label>
        <input
          id="filterDate"
          type="date"
          value={filterDate}
          onChange={(e) => setFilterDate(e.target.value)}
        />
        <label htmlFor="sortOrder">Sort Order:</label>
        <select
          id="sortOrder"
          value={sortOrder}
          onChange={(e) => setSortOrder(e.target.value)}
        >
          <option value="DESC">Latest to Earliest</option>
          <option value="ASC">Earliest to Latest</option>
        </select>
      </div>
      <div id="schedules"  className='schedules'>
        <h3>Scheduled Shuttles</h3>
        <table className="schedule-table">
          <thead>
            <tr>
              <th>booked Seats</th>
              <th>Route</th>
              <th>Date</th>
              <th>Time</th>
              <th>Bus</th>
            </tr>
          </thead>
          <tbody>
            {schedules.length > 0 ? (
              schedules.map((schedule) => (
                <tr key={schedule.id}>
                  <td>{schedule.totalSeatsBooked}</td>
                  <td>{schedule.direction || 'N/A'}</td>
                  <td>{schedule.date || 'N/A'}</td>
                  <td>{schedule.time || 'N/A'}</td>
                  <td>{schedule.bus || 'N/A'}</td>
                </tr>
              ))
            ) : (
              <tr className="no-data">
                <td colSpan="5">No data available for the selected filter</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
    </Layout>
  );
}

export default ViewShuttleSchedules;
