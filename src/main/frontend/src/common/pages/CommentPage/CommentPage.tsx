import React, {useEffect} from "react";
import {useAppSelector} from "../../../app/hooks";
import {selectAuthIsInitialized} from "../../../features/auth/authSlice";
import {Comment, useLazyGetAllCommentsQuery} from "../../../features/comment/commentApiSlice";
import styles from "./CommentPage.module.scss";

const CommentPage = () => {

    const isAuthInit = useAppSelector(selectAuthIsInitialized);

    // it's a Crutch. I need to prevent all requests that require auth until auth is initialized
    const [trigger, result, lastPromiseInfo] = useLazyGetAllCommentsQuery();

    const [comments, setComments] = React.useState<Comment[] | undefined>(undefined);

    useEffect(() => {
        if (isAuthInit) {
            trigger().then((res) => setComments(res.data));
        }
    }, [isAuthInit]);

    return (
        <main>
            {!comments && <p className={styles.loading}>Loading comments...</p>}
            <div className={styles.commentsWrapper}>
                {comments && comments.map((comment: Comment) => <CommentElement key={comment.id} {...comment}/>)}
            </div>
        </main>
    );
};

export default CommentPage;

const CommentElement = ({body, username, updatedAt}: Comment) => (
    <div className={styles.comment}>
        <h3 className={styles.username}>{username}</h3>
        <p className={styles.body}>{body}</p>
        {updatedAt && <p className={styles.updateAt}>Updated {transformUpdatedAt(new Date(updatedAt))}</p>}
    </div>
);

function transformUpdatedAt(date: Date) {

    const diff = new Date().getTime() - date.getTime();

    const diffInMinutes = Math.floor(diff / 1000 / 60);
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);
    const diffInWeeks = Math.floor(diffInDays / 7);
    const diffInMonths = Math.floor(diffInWeeks / 4);
    const diffInYears = Math.floor(diffInMonths / 12);

    if (diffInMinutes < 1) return "just now";
    if (diffInMinutes === 1) return "1 minute ago";
    if (diffInMinutes < 60) return `${diffInMinutes} minutes ago`;
    if (diffInHours === 1) return "1 hour ago";
    if (diffInHours < 24) return `${diffInHours} hours ago`;
    if (diffInDays === 1) return "1 day ago";
    if (diffInDays < 7) return `${diffInDays} days ago`;
    if (diffInWeeks === 1) return "1 week ago";
    if (diffInWeeks < 4) return `${diffInWeeks} weeks ago`;
    if (diffInMonths === 1) return "1 month ago";
    if (diffInMonths < 12) return `${diffInMonths} months ago`;
    if (diffInYears === 1) return "1 year ago";
    if (diffInYears < 100) return `${diffInYears} years ago`;
}
