import React, { useState, useRef } from 'react';
import {
  View,
  TextInput,
  Text,
  TouchableOpacity,
  StyleSheet,
  Animated,
  Platform,
} from 'react-native';
import { colors, spacing, radius, fontSize } from '../theme/tokens';

interface Props {
  label: string;
  placeholder: string;
  value: string;
  onChangeText: (v: string) => void;
  isPassword?: boolean;
  error?: string;
  returnKeyType?: 'next' | 'done' | 'go' | 'search';
  onSubmitEditing?: () => void;
  blurOnSubmit?: boolean;
  inputRef?: React.RefObject<TextInput>;
}

/** Styled input field matching the RescueLink HTML design */
export default function RescueLinkInput({
  label,
  placeholder,
  value,
  onChangeText,
  isPassword = false,
  error,
  returnKeyType = 'next',
  onSubmitEditing,
  blurOnSubmit = false,
  inputRef,
}: Props) {
  const [isFocused, setFocused] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const borderAnim = useRef(new Animated.Value(0)).current;

  const handleFocus = () => {
    setFocused(true);
    Animated.timing(borderAnim, {
      toValue: 1,
      duration: 200,
      useNativeDriver: false,
    }).start();
  };
  const handleBlur = () => {
    setFocused(false);
    Animated.timing(borderAnim, {
      toValue: 0,
      duration: 200,
      useNativeDriver: false,
    }).start();
  };

  const borderColor = borderAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [error ? colors.errorRed : colors.blackRim, colors.redHot],
  });

  const bgColor = borderAnim.interpolate({
    inputRange: [0, 1],
    outputRange: [colors.blackMid, colors.fieldFocusBg],
  });

  return (
    <View style={styles.fieldWrap}>
      <Text style={styles.label}>{label.toUpperCase()}</Text>
      <Animated.View
        style={[
          styles.inputWrap,
          { borderColor: error ? colors.errorRed : borderColor, backgroundColor: bgColor },
        ]}
      >
        <TextInput
          ref={inputRef}
          style={styles.input}
          placeholder={placeholder}
          placeholderTextColor={colors.whiteHint}
          value={value}
          onChangeText={onChangeText}
          secureTextEntry={isPassword && !showPassword}
          onFocus={handleFocus}
          onBlur={handleBlur}
          returnKeyType={returnKeyType}
          onSubmitEditing={onSubmitEditing}
          blurOnSubmit={blurOnSubmit}
          autoCapitalize="none"
          autoCorrect={false}
        />
        {isPassword && (
          <TouchableOpacity
            onPress={() => setShowPassword(s => !s)}
            style={styles.eyeBtn}
            accessibilityLabel={showPassword ? 'Hide password' : 'Show password'}
          >
            <Text style={styles.eyeIcon}>{showPassword ? '🙈' : '👁'}</Text>
          </TouchableOpacity>
        )}
      </Animated.View>
      {!!error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  fieldWrap: {
    marginBottom: spacing.lg,
  },
  label: {
    fontSize: fontSize.label,
    fontWeight: '600',
    letterSpacing: 2,
    color: colors.whiteLabel,
    marginBottom: spacing.sm,
  },
  inputWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1.5,
    borderRadius: radius.field,
    paddingHorizontal: spacing.lg,
    paddingVertical: Platform.OS === 'ios' ? 14 : 0,
  },
  input: {
    flex: 1,
    fontSize: fontSize.input,
    color: colors.whitePure,
    fontFamily: undefined, // will use Outfit when fonts are loaded
  },
  eyeBtn: {
    padding: spacing.xs,
  },
  eyeIcon: {
    fontSize: 16,
  },
  errorText: {
    fontSize: fontSize.error,
    color: colors.errorRed,
    marginTop: 4,
    paddingLeft: spacing.lg,
  },
});
