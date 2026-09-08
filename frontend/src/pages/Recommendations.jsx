import { useEffect, useState } from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { recommendationService } from '../services/recommendationService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { TrendingUp, Lightbulb, Shield, CheckCircle, Loader2 } from 'lucide-react';
import { format } from 'date-fns';

const Recommendations = () => {
  const { user } = useAuthStore();
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRecommendations();
  }, [user?.id]);

  const loadRecommendations = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);
      const userRecommendations = await recommendationService.getUserRecommendations(user.id);
      setRecommendations(userRecommendations);
    } catch (error) {
      console.error('Failed to load recommendations:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          AI Recommendations
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-2">
          Personalized insights powered by Google Gemini AI
        </p>
      </div>

      {recommendations.length === 0 ? (
        <Card>
          <CardContent className="text-center py-12">
            <TrendingUp className="h-16 w-16 mx-auto mb-4 text-gray-400" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
              No recommendations yet
            </h3>
            <p className="text-gray-500 dark:text-gray-400 mb-4">
              Track your activities to get AI-powered recommendations
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          {recommendations.map((recommendation) => (
            <Card key={recommendation.id} className="overflow-hidden">
              <CardHeader className="bg-gradient-to-r from-blue-50 to-purple-50 dark:from-gray-800 dark:to-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="flex items-center space-x-2">
                      <TrendingUp className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                      <span>{recommendation.activityType} Analysis</span>
                    </CardTitle>
                    <CardDescription>
                      {format(new Date(recommendation.createdAt), 'MMM dd, yyyy HH:mm')}
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="pt-6">
                <Tabs defaultValue="analysis" className="w-full">
                  <TabsList className="grid w-full grid-cols-3">
                    <TabsTrigger value="analysis">Analysis</TabsTrigger>
                    <TabsTrigger value="improvements">Improvements</TabsTrigger>
                    <TabsTrigger value="suggestions">Suggestions</TabsTrigger>
                  </TabsList>

                  <TabsContent value="analysis" className="space-y-4 mt-4">
                    <div className="prose dark:prose-invert max-w-none">
                      <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">
                        Performance Analysis
                      </h4>
                      <div className="bg-gray-50 dark:bg-gray-800 p-4 rounded-lg">
                        <p className="text-gray-700 dark:text-gray-300 whitespace-pre-line">
                          {recommendation.recommendation}
                        </p>
                      </div>
                    </div>
                  </TabsContent>

                  <TabsContent value="improvements" className="space-y-4 mt-4">
                    <div>
                      <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-3 flex items-center">
                        <Lightbulb className="h-5 w-5 mr-2 text-yellow-500" />
                        Areas for Improvement
                      </h4>
                      <div className="space-y-3">
                        {recommendation.improvements?.map((improvement, index) => (
                          <div
                            key={index}
                            className="flex items-start space-x-3 p-3 bg-gray-50 dark:bg-gray-800 rounded-lg"
                          >
                            <CheckCircle className="h-5 w-5 text-green-500 mt-0.5 flex-shrink-0" />
                            <p className="text-gray-700 dark:text-gray-300">{improvement}</p>
                          </div>
                        ))}
                      </div>
                    </div>
                  </TabsContent>

                  <TabsContent value="suggestions" className="space-y-4 mt-4">
                    <div>
                      <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-3 flex items-center">
                        <TrendingUp className="h-5 w-5 mr-2 text-blue-500" />
                        Workout Suggestions
                      </h4>
                      <div className="space-y-3">
                        {recommendation.suggestions?.map((suggestion, index) => (
                          <div
                            key={index}
                            className="p-4 bg-gradient-to-r from-blue-50 to-purple-50 dark:from-gray-800 dark:to-gray-700 rounded-lg"
                          >
                            <p className="text-gray-700 dark:text-gray-300">{suggestion}</p>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div>
                      <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-3 flex items-center">
                        <Shield className="h-5 w-5 mr-2 text-red-500" />
                        Safety Guidelines
                      </h4>
                      <div className="space-y-2">
                        {recommendation.safety?.map((guideline, index) => (
                          <div
                            key={index}
                            className="flex items-center space-x-2 text-sm text-gray-600 dark:text-gray-400"
                          >
                            <Shield className="h-4 w-4 text-red-400" />
                            <span>{guideline}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </TabsContent>
                </Tabs>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default Recommendations;
