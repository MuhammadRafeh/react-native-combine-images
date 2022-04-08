import { NativeModules, PermissionsAndroid, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-combine-images' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const CombineImages = NativeModules.CombineImages
  ? NativeModules.CombineImages
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

const permissions = [
  PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
  PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE
];

const normalizeFilePath = (path: string) => (path.startsWith('file://') ? path.slice(7) : path);

export async function combineImages(imagesPath: Array<string>, direction: string, imagesWidth: number, imagesHeight: number, saveToGallery: boolean): Promise<string> {
  const grants = await PermissionsAndroid.requestMultiple(permissions);
  if (grants['android.permission.WRITE_EXTERNAL_STORAGE'] != PermissionsAndroid.RESULTS.GRANTED || grants['android.permission.READ_EXTERNAL_STORAGE'] != PermissionsAndroid.RESULTS.GRANTED) return 'failed';
  return CombineImages.combineImages(imagesPath.map(path => normalizeFilePath(path)), direction ? direction : 'v', imagesWidth ? imagesWidth : -1, imagesHeight ? imagesHeight : -1, saveToGallery == undefined ? false : saveToGallery);
}