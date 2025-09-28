import { Box, Button, FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'
import React, { useState } from 'react'
import { addActivity } from '../services/api'


const ActivityForm = ({ onActivityAdded }) => {

    const [activity, setActivity] = useState({
        type: "RUNNING", 
        duration: 0, 
        caloriesBurnt: 0,
        startTime: new Date().toISOString()
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // Ensure duration and caloriesBurnt are integers
            const activityData = {
                ...activity,
                duration: parseInt(activity.duration) || 0,
                caloriesBurnt: parseInt(activity.caloriesBurnt) || 0,
                startTime: new Date().toISOString()
            };
            
            await addActivity(activityData);
            if (onActivityAdded) {
                onActivityAdded();
            }
            setActivity({ 
                type: "RUNNING", 
                duration: 0, 
                caloriesBurnt: 0,
                startTime: new Date().toISOString()
            });
        } catch (error) {
            console.error('Error adding activity:', error);
            console.error('Response data:', error.response?.data);
            console.error('Response status:', error.response?.status);
        }
    }
    
  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ mb: 4 }}>
    <FormControl fullWidth sx={{mb: 2}}>
        <InputLabel>Activity Type</InputLabel>
        <Select
            value={activity.type}
            onChange={(e) => setActivity({...activity, type: e.target.value})}>
                <MenuItem value="RUNNING">Running</MenuItem>
                <MenuItem value="WALKING">Walking</MenuItem>
                <MenuItem value="CYCLING">Cycling</MenuItem>
                <MenuItem value="WEIGHT_LIFTING">Weight Lifting</MenuItem>
                <MenuItem value="SWIMMING">Swimming</MenuItem>
                <MenuItem value="HIIT">HIIT</MenuItem>
                <MenuItem value="YOGA">Yoga</MenuItem>
                <MenuItem value="CARDIO">Cardio</MenuItem>
                <MenuItem value="STRENGTH_TRAINING">Strength Training</MenuItem>
                <MenuItem value="OTHER">Other</MenuItem>
            </Select>
    </FormControl>
    <TextField fullWidth
                label="Duration (Minutes)"
                type='number'
                sx={{ mb: 2}}
                value={activity.duration}
                onChange={(e) => setActivity({...activity, duration: parseInt(e.target.value) || 0})}/>

<TextField fullWidth
                label="Calories Burned"
                type='number'
                sx={{ mb: 2}}
                value={activity.caloriesBurnt}
                onChange={(e) => setActivity({...activity, caloriesBurnt: parseInt(e.target.value) || 0})}/>

<Button type='submit' variant='contained'>
    Add Activity
</Button>
  </Box>
  )
}

export default ActivityForm