/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import type {Node} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  useColorScheme,
  View,
  PermissionsAndroid,
  NativeModules,
} from 'react-native';

import {Colors, Header} from 'react-native/Libraries/NewAppScreen';
import {useEffect} from 'react/cjs/react.development';

const App: () => Node = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };
  const requestPermissions = async () => {
    try {
      const permissions = [PermissionsAndroid.PERMISSIONS.CAMERA];
      const granted = await PermissionsAndroid.request(permissions, {
        title: '我要相机权限',
        message: '没权限搞毛给我开',
      });
      console.log(granted);
      return granted;
    } catch (err) {
      return null;
    }
  };
  const ToolModule =  NativeModules.ToolModule : null;
  function get() {
    return ToolModule.getLocation();
  }

  useEffect(() => {
    requestPermissions();
    get();
  }, []);

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <View />
      </ScrollView>
    </SafeAreaView>
  );
};

export default App;
