import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { colors, fontSize } from '../theme/tokens';

/** "RescueLink" in large display font + "EMERGENCY ROAD ASSIST" tag */
export default function BrandBlock() {
  return (
    <View style={styles.container}>
      <Text style={styles.brandName}>RescueLink</Text>
      <Text style={styles.brandTag}>Emergency Road Assist</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    marginBottom: 0,
  },
  brandName: {
    fontSize: fontSize.brandName,
    letterSpacing: 3,
    fontWeight: '700',
    color: colors.whitePure,
    // Note: gradient text requires react-native-masked-view + linear-gradient.
    // Implemented as plain white here; replace with MaskedView if desired.
  },
  brandTag: {
    fontSize: fontSize.brandTag,
    fontWeight: '500',
    letterSpacing: 4,
    textTransform: 'uppercase',
    color: colors.redHot,
    marginTop: 4,
  },
});
