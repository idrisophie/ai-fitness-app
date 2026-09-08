import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useActivityStore } from '../store/useActivityStore';
import { activityService } from '../services/activityService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { Activity, Plus, Clock, Flame, Calendar } from 'lucide-react';
import { format } from 'date-fns';

const Activities = () => {
  const { user } = useAuthStore();
  const { activities, setActivities, setLoading, addActivity } = useActivityStore();
  const navigate = useNavigate();
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    type: '',
    duration: '',
    caloriesBurned: '',
    startTime: new Date().toISOString().slice(0, 16),
  });

  useEffect(() => {
    loadActivities();
  }, [user?.id]);

  const loadActivities = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);
      const userActivities = await activityService.getUserActivities(user.id);
      setActivities(userActivities);
    } catch (error) {
      console.error('Failed to load activities:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const activityData = {
        ...formData,
        userId: user.id,
        duration: parseInt(formData.duration),
        caloriesBurned: parseInt(formData.caloriesBurned),
        startTime: new Date(formData.startTime).toISOString(),
      };

      const newActivity = await activityService.trackActivity(activityData);
      addActivity(newActivity);
      setShowForm(false);
      setFormData({
        type: '',
        duration: '',
        caloriesBurned: '',
        startTime: new Date().toISOString().slice(0, 16),
      });
    } catch (error) {
      console.error('Failed to track activity:', error);
    }
  };

  const activityTypes = [
    'Running',
    'Cycling',
    'Swimming',
    'Weight Training',
    'Yoga',
    'HIIT',
    'Walking',
    'Other',
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Activities</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-2">
            Track and manage your workouts
          </p>
        </div>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? (
            'Cancel'
          ) : (
            <>
              <Plus className="h-4 w-4 mr-2" />
              Track Activity
            </>
          )}
        </Button>
      </div>

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle>Track New Activity</CardTitle>
            <CardDescription>
              Log your workout to get AI-powered recommendations
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="type">Activity Type</Label>
                <Select
                  value={formData.type}
                  onValueChange={(value) => setFormData({ ...formData, type: value })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select activity type" />
                  </SelectTrigger>
                  <SelectContent>
                    {activityTypes.map((type) => (
                      <SelectItem key={type} value={type}>
                        {type}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="duration">Duration (minutes)</Label>
                  <div className="relative">
                    <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <Input
                      id="duration"
                      type="number"
                      placeholder="30"
                      value={formData.duration}
                      onChange={(e) => setFormData({ ...formData, duration: e.target.value })}
                      className="pl-10"
                      required
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="calories">Calories Burned</Label>
                  <div className="relative">
                    <Flame className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <Input
                      id="calories"
                      type="number"
                      placeholder="200"
                      value={formData.caloriesBurned}
                      onChange={(e) => setFormData({ ...formData, caloriesBurned: e.target.value })}
                      className="pl-10"
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="startTime">Start Time</Label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <Input
                    id="startTime"
                    type="datetime-local"
                    value={formData.startTime}
                    onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                    className="pl-10"
                    required
                  />
                </div>
              </div>

              <Button type="submit" className="w-full">
                <Activity className="h-4 w-4 mr-2" />
                Track Activity
              </Button>
            </form>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {activities.map((activity) => (
          <Card
            key={activity.id}
            className="hover:shadow-lg transition-shadow cursor-pointer"
            onClick={() => navigate(`/activities/${activity.id}`)}
          >
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span className="flex items-center space-x-2">
                  <Activity className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                  <span>{activity.type}</span>
                </span>
              </CardTitle>
              <CardDescription>
                {format(new Date(activity.createdAt), 'MMM dd, yyyy HH:mm')}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Duration</p>
                  <p className="font-semibold text-gray-900 dark:text-white">
                    {activity.duration} min
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Calories</p>
                  <p className="font-semibold text-gray-900 dark:text-white">
                    {activity.caloriesBurned}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {activities.length === 0 && !showForm && (
        <Card>
          <CardContent className="text-center py-12">
            <Activity className="h-16 w-16 mx-auto mb-4 text-gray-400" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
              No activities yet
            </h3>
            <p className="text-gray-500 dark:text-gray-400 mb-4">
              Start tracking your workouts to see them here
            </p>
            <Button onClick={() => setShowForm(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Track Your First Activity
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default Activities;
