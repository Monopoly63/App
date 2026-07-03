declare module "jsmediatags/dist/jsmediatags.min.js" {
  export interface PictureTag {
    format: string;
    data: number[];
  }

  export interface Tags {
    title?: string;
    artist?: string;
    album?: string;
    picture?: PictureTag;
    [key: string]: unknown;
  }

  export interface TagResult {
    type: string;
    tags: Tags;
  }

  export interface ReadCallbacks {
    onSuccess: (result: TagResult) => void;
    onError: (error: unknown) => void;
  }

  const jsmediatags: {
    read: (file: File | Blob | string, callbacks: ReadCallbacks) => void;
  };

  export default jsmediatags;
}
