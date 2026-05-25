import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput, Dimensions, Animated, Easing } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Svg, { Path, Circle, Rect, G, Text as SvgText, Defs, Pattern } from 'react-native-svg';
import { colors, spacing, radius, fontSize } from '../theme/tokens';
import BackgroundLayer from '../components/BackgroundLayer';

const { width } = Dimensions.get('window');

export default function RescueLinkDashboard() {
  const [currentRoute, setCurrentRoute] = useState('dashboard');
  
  return (
    <View style={styles.container}>
      <BackgroundLayer />
      
      <View style={styles.topBar}>
        <View>
          <Text style={styles.brandTitle}>RescueLink</Text>
          <Text style={styles.brandSub}>EMERGENCY ROAD ASSIST</Text>
        </View>
        <View style={styles.liveBadge}>
          <View style={styles.liveDot} />
          <Text style={styles.liveText}>Live Tracking ON</Text>
        </View>
        <TouchableOpacity style={styles.notifBtn}>
          <Text style={{color: colors.whiteDim}}>🔔</Text>
          <View style={styles.notifDot} />
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
        <GreetingRow name="Rahul" />
        <TowBanner />
        <StatCardsGrid />
        <MapCard />
        <NearbyServicesCard />
        <AiChatCard />
        <RecentIncidentsCard />
        <CommunityFeedCard />
      </ScrollView>

      <BottomNav currentRoute={currentRoute} onRoute={setCurrentRoute} />
    </View>
  );
}

// ─── GREETING ─────────────────────────────────────────────────────────────
const GreetingRow = ({ name }: { name: string }) => (
  <View style={styles.greetingRow}>
    <View style={{flex: 1}}>
      <Text style={styles.greetingTitle}>Good Evening, <Text style={{color: colors.redHot}}>{name}</Text> 👋</Text>
      <Text style={styles.greetingSub}>Your vehicle is tracked & 3 mechanics are nearby. Stay safe.</Text>
    </View>
    <TouchableOpacity style={styles.sosBtn}>
      <LinearGradient colors={[colors.redHot, colors.redDeep]} style={styles.sosGradient} start={{x: 0, y: 0}} end={{x: 1, y: 1}}>
        <View style={styles.sosDot} />
        <Text style={styles.sosBtnText}>SOS Emergency</Text>
      </LinearGradient>
    </TouchableOpacity>
  </View>
);

// ─── TOW BANNER ───────────────────────────────────────────────────────────
const TowBanner = () => {
  const [progress] = useState(new Animated.Value(0));
  
  useEffect(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(progress, { toValue: 1, duration: 2000, useNativeDriver: false }),
        Animated.timing(progress, { toValue: 0, duration: 2000, useNativeDriver: false })
      ])
    ).start();
  }, []);

  return (
    <View style={styles.towBanner}>
      <View style={styles.towIconBox}><Text style={{fontSize: 20}}>🚛</Text></View>
      <View style={styles.towInfo}>
        <Text style={styles.towTitle}>Tow Truck En Route — Sharma Towing Co.</Text>
        <Text style={styles.towSub}>Driver Vikram Singh • Mahindra Bolero • MH-12</Text>
        <View style={styles.progressTrack}>
          <Animated.View style={[styles.progressFill, { width: '62%', opacity: progress.interpolate({inputRange:[0,1], outputRange:[0.6,1]}) }]} />
        </View>
      </View>
      <View style={styles.towEtaBox}>
        <Text style={styles.towEtaVal}>12</Text>
        <Text style={styles.towEtaLabel}>MIN ETA</Text>
      </View>
    </View>
  );
};

// ─── STAT CARDS ───────────────────────────────────────────────────────────
const StatCardsGrid = () => (
  <View style={styles.statGrid}>
    <View style={styles.statRow}>
      <StatCard icon="🔧" value="3" label="Mechanics Nearby" change="↑ 2 new" color={colors.green} />
      <StatCard icon="🚛" value="1" label="Active Tow Request" change="In Progress" color={colors.orange} />
    </View>
    <View style={styles.statRow}>
      <StatCard icon="👥" value="24" label="Community Members" change="↑ 5 online" color={colors.green} />
      <StatCard icon="⚠️" value="7" label="Total Past Incidents" change="last: 3d ago" color={colors.redHot} isDown />
    </View>
  </View>
);

const StatCard = ({ icon, value, label, change, color, isDown }: any) => (
  <View style={styles.statCard}>
    <LinearGradient colors={['rgba(232,0,29,0.4)', 'transparent']} start={{x:0, y:0}} end={{x:1, y:0}} style={styles.statTopLine} />
    <View style={styles.statIconBox}><Text style={{fontSize: 18}}>{icon}</Text></View>
    <Text style={styles.statVal}>{value}</Text>
    <Text style={styles.statLabel}>{label}</Text>
    <View style={[styles.statBadge, {backgroundColor: isDown ? colors.redDim : color + '22'}]}>
      <Text style={[styles.statBadgeText, {color: isDown ? colors.redHot : color}]}>{change}</Text>
    </View>
  </View>
);

// ─── MAP CARD ─────────────────────────────────────────────────────────────
const MapCard = () => {
  const [anim] = useState(new Animated.Value(0));
  useEffect(() => {
    Animated.loop(Animated.timing(anim, { toValue: 1, duration: 3000, easing: Easing.linear, useNativeDriver: true })).start();
  }, []);

  const truckX = anim.interpolate({ inputRange: [0, 0.5, 1], outputRange: [0, 40, 0] });
  const pinY = anim.interpolate({ inputRange: [0, 0.25, 0.5, 0.75, 1], outputRange: [0, -6, 0, -6, 0] });

  return (
    <View style={styles.cardContainer}>
      <View style={styles.cardHeader}>
        <View>
          <Text style={styles.cardTitle}>Live Location Map</Text>
          <Text style={styles.cardSub}>NH-48, Near Mansa, Gujarat</Text>
        </View>
        <View style={styles.gpsBadge}><View style={styles.gpsDot}/><Text style={styles.gpsText}>GPS Active</Text></View>
      </View>
      <View style={styles.mapBody}>
        <Svg width="100%" height={220} viewBox="0 0 420 220" preserveAspectRatio="xMidYMid slice">
          <Rect width="100%" height="100%" fill="#0d0d0d" />
          <Defs>
            <Pattern id="grid" width="30" height="30" patternUnits="userSpaceOnUse">
              <Path d="M 30 0 L 0 0 0 30" fill="none" stroke="rgba(255,255,255,0.03)" strokeWidth="0.5" />
            </Pattern>
          </Defs>
          <Rect width="100%" height="100%" fill="url(#grid)" />
          
          <Path d="M0,110 Q105,95 210,110 Q315,125 420,110" stroke={colors.blackRim} strokeWidth="14" fill="none" />
          <Path d="M210,0 Q195,55 210,110 Q225,165 210,220" stroke={colors.blackRim} strokeWidth="14" fill="none" />
          
          <Path d="M0,110 Q105,95 210,110 Q315,125 420,110" stroke="rgba(232,0,29,0.15)" strokeWidth="2" strokeDasharray="8 8" fill="none" />
          <Path d="M210,0 Q195,55 210,110 Q225,165 210,220" stroke="rgba(232,0,29,0.15)" strokeWidth="2" strokeDasharray="8 8" fill="none" />

          {/* User Pin */}
          <Circle cx="210" cy="110" r="18" fill="none" stroke="rgba(232,0,29,0.3)" strokeWidth="1.5" />
          <G transform={[{translateY: pinY as any}]}>
            <Circle cx="210" cy="110" r="10" fill="rgba(232,0,29,0.2)" stroke="rgba(232,0,29,0.5)" strokeWidth="1.5" />
            <Circle cx="210" cy="110" r="5" fill={colors.redHot} />
            <Circle cx="210" cy="110" r="2" fill="white" />
          </G>

          {/* Mechanics */}
          <Rect x="134" y="63" width="22" height="22" rx="6" fill="#1a3a1a" stroke={colors.green} strokeWidth="1.5" />
          <SvgText x="145" y="78" fontSize="11" fill={colors.green} textAnchor="middle">🔧</SvgText>
          
          <Rect x="267" y="73" width="22" height="22" rx="6" fill="#1a3a1a" stroke={colors.green} strokeWidth="1.5" />
          <SvgText x="278" y="88" fontSize="11" fill={colors.green} textAnchor="middle">🔧</SvgText>

          {/* Tow Truck */}
          <G transform={[{translateX: truckX as any}]}>
            <Rect x="148" y="98" width="22" height="22" rx="6" fill="#1a0004" stroke={colors.redHot} strokeWidth="1.5" />
            <SvgText x="159" y="113" fontSize="11" textAnchor="middle">🚛</SvgText>
          </G>
        </Svg>
        <View style={styles.mapLegend}>
          <LegendItem color={colors.redHot} label="You" />
          <LegendItem color={colors.green} label="Mechanic" />
          <LegendItem color={colors.orange} label="Tow Truck" />
        </View>
      </View>
    </View>
  );
};

const LegendItem = ({ color, label }: any) => (
  <View style={styles.legendItem}>
    <View style={[styles.legendDot, {backgroundColor: color}]} />
    <Text style={styles.legendText}>{label}</Text>
  </View>
);

// ─── SERVICES ─────────────────────────────────────────────────────────────
const NearbyServicesCard = () => (
  <View style={styles.cardContainer}>
    <View style={styles.cardHeader}>
      <Text style={styles.cardTitle}>Nearby Services</Text>
      <Text style={styles.seeAll}>See All</Text>
    </View>
    <View style={styles.cardBody}>
      <ServiceRow icon="🔧" name="Patel Auto Workshop" meta="★★★★★  Open Now" dist="1.2 km" eta="~4 min" color={colors.green} />
      <ServiceRow icon="🔧" name="Singh Motors & Repair" meta="★★★★☆  Open Now" dist="2.8 km" eta="~9 min" color={colors.green} />
      <ServiceRow icon="🚛" name="Sharma Towing Co." meta="★★★★★  En Route" dist="Active" eta="12 min ETA" color={colors.redHot} isWarn />
      <ServiceRow icon="⛽" name="NH-48 Petrol Station" meta="★★★☆☆  24 hrs" dist="0.8 km" eta="~2 min" color={colors.blue} />
    </View>
  </View>
);

const ServiceRow = ({ icon, name, meta, dist, eta, color, isWarn }: any) => (
  <View style={styles.serviceRow}>
    <View style={[styles.serviceIconBox, {backgroundColor: color + '22', borderColor: color + '44'}]}><Text>{icon}</Text></View>
    <View style={styles.serviceInfo}>
      <Text style={styles.serviceName}>{name}</Text>
      <Text style={styles.serviceMeta}>{meta}</Text>
    </View>
    <View style={{alignItems: 'flex-end'}}>
      <Text style={[styles.serviceDist, {color: isWarn ? colors.orange : color}]}>{dist}</Text>
      <Text style={styles.serviceEta}>{eta}</Text>
    </View>
  </View>
);

// ─── AI CHAT ──────────────────────────────────────────────────────────────
const AiChatCard = () => {
  const [msgs, setMsgs] = useState([
    { isAi: true, text: "Hey Rahul! I see your vehicle is stopped on NH-48. I've already dispatched the nearest tow truck. How can I help further?" },
    { isAi: false, text: "Engine won't start. Battery or alternator?" },
    { isAi: true, text: "Likely dead battery. Try jump-starting — I've notified Patel Auto Workshop 1.2km away." }
  ]);
  const [inp, setInp] = useState('');

  return (
    <View style={styles.cardContainer}>
      <View style={styles.cardHeader}>
        <View>
          <Text style={styles.cardTitle}>AI Assistant</Text>
          <Text style={styles.cardSub}>RESCUE AI • Online</Text>
        </View>
        <View style={[styles.gpsDot, {backgroundColor: colors.green}]} />
      </View>
      <View style={{height: 240, padding: 14}}>
        <ScrollView style={{flex: 1}} showsVerticalScrollIndicator={false}>
          {msgs.map((m, i) => (
            <View key={i} style={[styles.msgContainer, m.isAi ? {alignItems:'flex-start'} : {alignItems:'flex-end'}]}>
              <Text style={styles.msgSender}>{m.isAi ? 'RESCUE AI' : 'YOU'}</Text>
              <View style={[styles.msgBubble, m.isAi ? styles.msgAi : styles.msgUser]}>
                <Text style={styles.msgText}>{m.text}</Text>
              </View>
            </View>
          ))}
        </ScrollView>
      </View>
      <View style={styles.chatInputRow}>
        <TextInput style={styles.chatInput} placeholder="Ask anything..." placeholderTextColor={colors.whiteDim} value={inp} onChangeText={setInp} />
        <TouchableOpacity style={styles.chatSend} onPress={() => { if(inp) { setMsgs([...msgs, {isAi:false, text:inp}]); setInp(''); } }}>
          <Text style={{color:'white'}}>➤</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

// ─── INCIDENTS & FEED ─────────────────────────────────────────────────────
const RecentIncidentsCard = () => (
  <View style={styles.cardContainer}>
    <View style={styles.cardHeader}><Text style={styles.cardTitle}>Recent Incidents</Text><Text style={styles.seeAll}>View All</Text></View>
    <View style={styles.cardBody}>
      <IncidentRow color={colors.redHot} title="Engine Breakdown — NH-48" sub="Tow requested" time="Now" />
      <IncidentRow color={colors.green} title="Flat Tyre — Ahmedabad Hwy" sub="Resolved by Patel Workshop" time="3d ago" />
      <IncidentRow color={colors.green} title="Fuel Empty — Gandhinagar Rd" sub="Community help" time="9d ago" />
    </View>
  </View>
);

const IncidentRow = ({ color, title, sub, time }: any) => (
  <View style={styles.serviceRow}>
    <View style={[styles.gpsDot, {backgroundColor: color}]} />
    <View style={styles.serviceInfo}>
      <Text style={styles.serviceName}>{title}</Text>
      <Text style={styles.serviceMeta}>{sub}</Text>
    </View>
    <Text style={styles.serviceEta}>{time}</Text>
  </View>
);

const CommunityFeedCard = () => (
  <View style={styles.cardContainer}>
    <View style={styles.cardHeader}><Text style={styles.cardTitle}>Community Feed</Text><Text style={styles.seeAll}>See All</Text></View>
    <View style={styles.cardBody}>
      <CommunityRow in="AS" name="Amit Shah" time="2 min ago" msg="Traffic jam on NH-48 near Mansa — accident reported ahead." up="14" />
      <CommunityRow in="PV" name="Priya Verma" time="18 min ago" msg="Anyone near Mahesana knows a good diesel mechanic?" up="3" />
    </View>
  </View>
);

const CommunityRow = ({ in: ini, name, time, msg, up }: any) => (
  <View style={styles.serviceRow}>
    <View style={styles.avatar}><Text style={{color:'white', fontSize: 10, fontWeight: 'bold'}}>{ini}</Text></View>
    <View style={styles.serviceInfo}>
      <View style={{flexDirection:'row', justifyContent:'space-between'}}>
        <Text style={styles.serviceName}>{name}</Text>
        <Text style={styles.serviceEta}>{time}</Text>
      </View>
      <Text style={[styles.serviceMeta, {marginTop: 4}]} numberOfLines={2}>{msg}</Text>
      <Text style={[styles.serviceEta, {marginTop: 4, textAlign: 'left'}]}>▲ {up} helpful</Text>
    </View>
  </View>
);

// ─── BOTTOM NAV ───────────────────────────────────────────────────────────
const BottomNav = ({ currentRoute, onRoute }: any) => {
  const items = [
    { id: 'dashboard', icon: '🏠', label: 'Home' },
    { id: 'map', icon: '📍', label: 'Map' },
    { id: 'community', icon: '👥', label: 'Community' },
    { id: 'chat', icon: '💬', label: 'AI Chat' },
  ];
  return (
    <View style={styles.bottomNav}>
      {items.map(it => (
        <TouchableOpacity key={it.id} style={styles.navItem} onPress={() => onRoute(it.id)}>
          <Text style={[styles.navIcon, currentRoute === it.id && {color: colors.redHot}]}>{it.icon}</Text>
          <Text style={[styles.navLabel, currentRoute === it.id && {color: colors.redHot}]}>{it.label}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};

// ─── STYLES ───────────────────────────────────────────────────────────────
const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.blackCore },
  topBar: { height: 64, flexDirection: 'row', alignItems: 'center', paddingHorizontal: 24, backgroundColor: 'rgba(8,8,8,0.85)', zIndex: 10 },
  brandTitle: { fontFamily: 'Bebas Neue', fontSize: 22, color: colors.whitePure, letterSpacing: 1 },
  brandSub: { fontFamily: 'Outfit', fontSize: 10, color: colors.white35, fontWeight: '500' },
  liveBadge: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.redDim, paddingHorizontal: 12, paddingVertical: 4, borderRadius: 100, marginLeft: 'auto', marginRight: 16 },
  liveDot: { width: 6, height: 6, borderRadius: 3, backgroundColor: colors.redHot, marginRight: 6 },
  liveText: { fontFamily: 'Outfit', fontSize: 10, color: colors.redHot, fontWeight: 'bold' },
  notifBtn: { width: 36, height: 36, backgroundColor: colors.blackCard, borderRadius: 10, borderWidth: 1, borderColor: colors.blackRim, alignItems: 'center', justifyContent: 'center' },
  notifDot: { position: 'absolute', top: 6, right: 6, width: 6, height: 6, backgroundColor: colors.redHot, borderRadius: 3, borderWidth: 1, borderColor: colors.blackCore },
  scrollContent: { padding: 20, gap: 16, paddingBottom: 100 },
  greetingRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: 8 },
  greetingTitle: { fontFamily: 'Outfit', fontSize: 24, fontWeight: '800', color: colors.whitePure },
  greetingSub: { fontFamily: 'Outfit', fontSize: 13, color: colors.whiteDim, marginTop: 4 },
  sosBtn: { height: 44, borderRadius: 13, overflow: 'hidden' },
  sosGradient: { flex: 1, flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16 },
  sosDot: { width: 8, height: 8, backgroundColor: 'white', borderRadius: 4, marginRight: 8 },
  sosBtnText: { fontFamily: 'Outfit', fontSize: 13, fontWeight: '700', color: 'white' },
  towBanner: { backgroundColor: colors.redDim, borderRadius: 14, borderWidth: 1, borderColor: 'rgba(232,0,29,0.3)', padding: 14, flexDirection: 'row', alignItems: 'center' },
  towIconBox: { width: 42, height: 42, backgroundColor: colors.redDim, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  towInfo: { flex: 1, marginHorizontal: 12 },
  towTitle: { fontFamily: 'Outfit', fontSize: 13, fontWeight: '700', color: colors.whitePure },
  towSub: { fontFamily: 'Outfit', fontSize: 11, color: colors.whiteDim, marginTop: 2 },
  progressTrack: { height: 3, backgroundColor: colors.blackRim, borderRadius: 2, marginTop: 8 },
  progressFill: { height: '100%', backgroundColor: colors.redHot, borderRadius: 2 },
  towEtaBox: { alignItems: 'center', backgroundColor: colors.redDim, paddingHorizontal: 12, paddingVertical: 6, borderRadius: 10 },
  towEtaVal: { fontFamily: 'Bebas Neue', fontSize: 22, color: colors.redHot },
  towEtaLabel: { fontFamily: 'Outfit', fontSize: 9, color: colors.white35, fontWeight: 'bold' },
  statGrid: { gap: 12 },
  statRow: { flexDirection: 'row', gap: 12 },
  statCard: { flex: 1, backgroundColor: colors.blackCard, borderRadius: 16, borderWidth: 1, borderColor: colors.blackRim, padding: 16 },
  statTopLine: { position: 'absolute', top: 0, left: 16, right: 16, height: 1 },
  statIconBox: { width: 34, height: 34, backgroundColor: colors.redDim, borderRadius: 10, alignItems: 'center', justifyContent: 'center', marginBottom: 12 },
  statVal: { fontFamily: 'Bebas Neue', fontSize: 32, color: colors.whitePure },
  statLabel: { fontFamily: 'Outfit', fontSize: 11, color: colors.whiteDim, fontWeight: '500' },
  statBadge: { position: 'absolute', top: 16, right: 12, paddingHorizontal: 6, paddingVertical: 2, borderRadius: 100 },
  statBadgeText: { fontFamily: 'Outfit', fontSize: 9, fontWeight: 'bold' },
  cardContainer: { backgroundColor: colors.blackCard, borderRadius: 16, borderWidth: 1, borderColor: colors.blackRim, overflow: 'hidden' },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: 16, borderBottomWidth: 1, borderBottomColor: colors.blackRim },
  cardTitle: { fontFamily: 'Outfit', fontSize: 14, fontWeight: 'bold', color: colors.whitePure },
  cardSub: { fontFamily: 'Outfit', fontSize: 11, color: colors.white35, marginTop: 2 },
  seeAll: { fontFamily: 'Outfit', fontSize: 12, fontWeight: '600', color: colors.redHot },
  gpsBadge: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.redDim, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 100 },
  gpsDot: { width: 6, height: 6, backgroundColor: colors.redHot, borderRadius: 3, marginRight: 4 },
  gpsText: { fontFamily: 'Outfit', fontSize: 9, color: colors.redHot, fontWeight: 'bold' },
  mapBody: { height: 220, backgroundColor: '#0d0d0d' },
  mapLegend: { position: 'absolute', bottom: 12, left: 12, flexDirection: 'row', gap: 8 },
  legendItem: { flexDirection: 'row', alignItems: 'center', backgroundColor: 'rgba(8,8,8,0.8)', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 100, borderWidth: 1, borderColor: colors.blackRim },
  legendDot: { width: 6, height: 6, borderRadius: 3, marginRight: 4 },
  legendText: { fontFamily: 'Outfit', fontSize: 9, color: colors.whiteDim, fontWeight: 'bold' },
  cardBody: { padding: 12, gap: 8 },
  serviceRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.blackMid, padding: 10, borderRadius: 12, borderWidth: 1, borderColor: colors.blackRim },
  serviceIconBox: { width: 34, height: 34, borderRadius: 10, alignItems: 'center', justifyContent: 'center', marginRight: 12 },
  serviceInfo: { flex: 1, marginRight: 8 },
  serviceName: { fontFamily: 'Outfit', fontSize: 12, fontWeight: 'bold', color: colors.whitePure },
  serviceMeta: { fontFamily: 'Outfit', fontSize: 10, color: colors.whiteDim, marginTop: 2 },
  serviceDist: { fontFamily: 'Outfit', fontSize: 11, fontWeight: 'bold', color: colors.green },
  serviceEta: { fontFamily: 'Outfit', fontSize: 10, color: colors.white35, marginTop: 2 },
  msgContainer: { maxWidth: '85%', marginBottom: 12 },
  msgSender: { fontFamily: 'Outfit', fontSize: 9, color: colors.white35, fontWeight: 'bold', marginBottom: 4 },
  msgBubble: { padding: 10, borderRadius: 12 },
  msgAi: { backgroundColor: colors.blackMid, borderBottomLeftRadius: 4, borderWidth: 1, borderColor: colors.blackRim },
  msgUser: { backgroundColor: colors.redHot, borderBottomRightRadius: 4 },
  msgText: { fontFamily: 'Outfit', fontSize: 12, color: colors.whitePure },
  chatInputRow: { flexDirection: 'row', padding: 12, borderTopWidth: 1, borderTopColor: colors.blackRim },
  chatInput: { flex: 1, height: 40, backgroundColor: colors.blackMid, borderRadius: 10, borderWidth: 1, borderColor: colors.blackRim, paddingHorizontal: 12, color: 'white', fontFamily: 'Outfit', fontSize: 12 },
  chatSend: { width: 40, height: 40, backgroundColor: colors.redHot, borderRadius: 10, alignItems: 'center', justifyContent: 'center', marginLeft: 8 },
  avatar: { width: 30, height: 30, borderRadius: 15, backgroundColor: colors.redDeep, alignItems: 'center', justifyContent: 'center', marginRight: 10 },
  bottomNav: { position: 'absolute', bottom: 0, left: 0, right: 0, height: 60, backgroundColor: colors.blackCard, borderTopWidth: 1, borderTopColor: colors.blackRim, flexDirection: 'row' },
  navItem: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  navIcon: { fontSize: 20, color: colors.white35 },
  navLabel: { fontFamily: 'Outfit', fontSize: 10, color: colors.white35, marginTop: 2 }
});
