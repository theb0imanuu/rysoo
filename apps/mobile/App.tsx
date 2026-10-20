import React, { useState } from 'react';
import { StyleSheet, View, SafeAreaView } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { DashboardScreen } from './src/screens/DashboardScreen';
import { StudioScreen } from './src/screens/StudioScreen';

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<'dashboard' | 'studio'>('dashboard');

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="light" />
      <View style={styles.container}>
        {currentScreen === 'dashboard' ? (
          <DashboardScreen onNavigateStudio={() => setCurrentScreen('studio')} />
        ) : (
          <StudioScreen onBack={() => setCurrentScreen('dashboard')} />
        )}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#0a0d14',
  },
  container: {
    flex: 1,
    backgroundColor: '#0a0d14',
  },
});
