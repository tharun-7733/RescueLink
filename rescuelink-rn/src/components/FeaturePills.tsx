import React from 'react';
import { View, Text, ScrollView, StyleSheet } from 'react-native';
import { colors, spacing, radius, fontSize } from '../theme/tokens';

const PILLS = [
  { label: 'Mechanic Finder', icon: '🔧' },
  { label: 'Tow Service',     icon: '🚛' },
  { label: 'Community',       icon: '👥' },
  { label: 'AI Assistant',    icon: '✨' },
];

/** Horizontally scrollable feature pills row */
export default function FeaturePills() {
  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.row}
    >
      {PILLS.map(pill => (
        <View key={pill.label} style={styles.pill}>
          <Text style={styles.icon}>{pill.icon}</Text>
          <Text style={styles.label}>{pill.label}</Text>
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    gap: spacing.sm,
    paddingHorizontal: 2,
  },
  pill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.whiteGhost,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.07)',
    borderRadius: radius.pill,
    paddingHorizontal: 14,
    paddingVertical: 7,
    gap: 6,
  },
  icon: {
    fontSize: 12,
  },
  label: {
    fontSize: fontSize.pillText,
    fontWeight: '500',
    color: colors.whitePure,
  },
});
