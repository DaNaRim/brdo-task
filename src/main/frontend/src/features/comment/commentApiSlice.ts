import {apiSlice} from "../api/apiSlice";

export type Comment = {
    id: number;
    body: string;
    postId: number;
    username: string;
    updatedAt: Date | null;
};

export const commentApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getAllComments: builder.query<Comment[], void>({
            query: () => ({
                url: "/comments",
            }),
        }),
    }),
});

export const {useLazyGetAllCommentsQuery} = commentApiSlice;
