import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export enum Role {
    ROLE_USER = "ROLE_USER",
    ROLE_ADMIN = "ROLE_ADMIN",
}

export type AuthResponseEntity = {
    username: string; //email
    roles: Role[];
    csrfToken: string;
}

export type AuthState = {
    username: string | null; //email
    roles: Role[];
    csrfToken: string | null;

    isInitialized: boolean;
    isForceLogin: boolean,
}

const initialState: AuthState = {
    username: null, //email
    roles: [],
    csrfToken: null,

    isInitialized: false,
    isForceLogin: false,
};

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        setCredentials(state, action: PayloadAction<AuthResponseEntity | AuthState>) {
            const response = action.payload;

            state.username = response.username;
            state.roles = response.roles;
            state.csrfToken = response.csrfToken;

            state.isInitialized = true;
        },
        setInitialized(state) {
            state.isInitialized = true;
        },
        clearAuthState(state) {
            state.username = null;
            state.roles = [];
            state.csrfToken = null;
        },
    },
});

export const {
    setCredentials,
    setInitialized,
    clearAuthState,
} = authSlice.actions;

export const selectAuthUsername = (state: RootState) => state.auth.username;
export const selectAuthIsInitialized = (state: RootState) => state.auth.isInitialized;

export default authSlice.reducer;
