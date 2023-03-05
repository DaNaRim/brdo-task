import React from "react";
import {Route, Routes} from "react-router";
import PageWrapper from "../common/components/base/PageWrapper/PageWrapper";
import "./App.scss";

const HomePage = React.lazy(() => import("../common/pages/HomePage/HomePage"));
const LoginPage = React.lazy(() => import("../common/pages/LoginPage/LoginPage"));
const CommentPage = React.lazy(() => import("../common/pages/CommentPage/CommentPage"));

const ForbiddenPage = React.lazy(() => import("../common/pages/errors/ForbiddenPage/ForbiddenPage"));
const ErrorPage = React.lazy(() => import("../common/pages/errors/ErrorPage/ErrorPage"));
const NotFoundPage = React.lazy(() => import("../common/pages/errors/NotFoundPage/NotFoundPage"));

const App = () => (
    <Routes>
        <Route path="/" element={<PageWrapper/>}>
            <Route path="/" index element={<HomePage/>}/>
            <Route path="/login" element={<LoginPage/>}/>
            <Route path="/comments" element={<CommentPage/>}/>

            <Route path="/forbidden" element={<ForbiddenPage/>}/>
            <Route path="/error" element={<ErrorPage/>}/>
            <Route path="*" element={<NotFoundPage/>}/>
        </Route>
    </Routes>
);

export default App;
