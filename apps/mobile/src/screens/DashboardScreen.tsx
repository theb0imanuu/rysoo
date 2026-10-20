import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image } from 'react-native';

interface DashboardScreenProps {
  onNavigateStudio: () => void;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({ onNavigateStudio }) => {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Brand Header */}
      <View style={styles.brandRow}>
        <Image
          source={require('../../assets/logo.png')}
          style={styles.logo}
          resizeMode="contain"
        />
        <View>
          <Text style={styles.brandTitle}>RYSOO</Text>
          <Text style={styles.brandSubtitle}>Fact-Certified ATS Engine</Text>
        </View>
      </View>

      {/* Hero Guarantee Card */}
      <View style={styles.card}>
        <View style={styles.badgeRow}>
          <Text style={styles.badgeText}>100% GROUNDING GUARANTEE</Text>
        </View>
        <Text style={styles.cardTitle}>Zero-Hallucination CV Tailoring</Text>
        <Text style={styles.cardBody}>
          Every employer, degree, metric, and skill is verified deterministically against your source document facts.
        </Text>
        <TouchableOpacity style={styles.primaryButton} onPress={onNavigateStudio}>
          <Text style={styles.primaryButtonText}>Launch Optimization Studio</Text>
        </TouchableOpacity>
      </View>

      {/* Metric Cards */}
      <View style={styles.statsGrid}>
        <View style={styles.statBox}>
          <Text style={styles.statNumber}>100%</Text>
          <Text style={styles.statLabel}>Factual Grounding</Text>
        </View>
        <View style={styles.statBox}>
          <Text style={styles.statNumber}>+38%</Text>
          <Text style={styles.statLabel}>ATS Score Boost</Text>
        </View>
      </View>

      {/* Document List */}
      <Text style={styles.sectionHeading}>Verified Documents</Text>

      <View style={styles.docCard}>
        <Text style={styles.docTitle}>Jane Doe - Senior Staff Engineer</Text>
        <Text style={styles.docSubtitle}>Jane_Doe_CV.pdf • 2 Employers • 14 Grounded Skills</Text>
        <TouchableOpacity style={styles.secondaryButton} onPress={onNavigateStudio}>
          <Text style={styles.secondaryButtonText}>Tailor for Job &rarr;</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.docCard}>
        <Text style={styles.docTitle}>Alex Chen - Principal Platform Engineer</Text>
        <Text style={styles.docSubtitle}>Alex_Chen_CV.docx • 2 Employers • 12 Grounded Skills</Text>
        <TouchableOpacity style={styles.secondaryButton} onPress={onNavigateStudio}>
          <Text style={styles.secondaryButtonText}>Tailor for Job &rarr;</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0a0d14',
  },
  content: {
    padding: 20,
    paddingBottom: 40,
  },
  brandRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 24,
    marginTop: 10,
  },
  logo: {
    width: 40,
    height: 40,
  },
  brandTitle: {
    color: '#ffffff',
    fontSize: 22,
    fontWeight: '800',
    letterSpacing: 1,
  },
  brandSubtitle: {
    color: '#94a3b8',
    fontSize: 12,
    fontWeight: '500',
  },
  card: {
    backgroundColor: 'rgba(18, 24, 38, 0.8)',
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    marginBottom: 20,
  },
  badgeRow: {
    alignSelf: 'flex-start',
    backgroundColor: 'rgba(16, 185, 129, 0.15)',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(16, 185, 129, 0.3)',
    marginBottom: 12,
  },
  badgeText: {
    color: '#10b981',
    fontSize: 10,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  cardTitle: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: '700',
    marginBottom: 8,
  },
  cardBody: {
    color: '#94a3b8',
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 16,
  },
  primaryButton: {
    backgroundColor: '#6366f1',
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center',
  },
  primaryButtonText: {
    color: '#ffffff',
    fontWeight: '700',
    fontSize: 14,
  },
  statsGrid: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 24,
  },
  statBox: {
    flex: 1,
    backgroundColor: 'rgba(18, 24, 38, 0.8)',
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    alignItems: 'center',
  },
  statNumber: {
    color: '#ffffff',
    fontSize: 22,
    fontWeight: '800',
    marginBottom: 4,
  },
  statLabel: {
    color: '#94a3b8',
    fontSize: 11,
    fontWeight: '500',
  },
  sectionHeading: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 12,
  },
  docCard: {
    backgroundColor: 'rgba(18, 24, 38, 0.6)',
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    marginBottom: 12,
  },
  docTitle: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 4,
  },
  docSubtitle: {
    color: '#64748b',
    fontSize: 11,
    marginBottom: 12,
  },
  secondaryButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 8,
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  secondaryButtonText: {
    color: '#a5b4fc',
    fontSize: 12,
    fontWeight: '600',
  },
});
