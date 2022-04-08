import { NativeModules, Platform } from 'react-native';

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

export function combineImages(imagesPath: Array<string>, direction: string, imagesWidth: number, imagesHeight: number, saveToGallery: boolean): Promise<number> {
  return CombineImages.combineImages(imagesPath, direction, imagesWidth, imagesHeight, saveToGallery);
}