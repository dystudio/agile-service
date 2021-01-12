import { createContext, useContext } from 'react';
import { DetailEvents } from '.';

export interface IRoute {
  path: string
  props?: Object
  events?: DetailEvents
}
interface DetailContainerContext {
  outside: boolean
  topAnnouncementHeight: number
  routes: IRoute[]
  match: IRoute
  open: (route: IRoute) => void
  push: (nextRoute: IRoute) => void
  pop: () => void
  close: () => void
  eventsMap: Map<string, DetailEvents>
}
const DetailContainerContext = createContext({} as DetailContainerContext);

function useDetailContainerContext() {
  const context = useContext(DetailContainerContext);
  return context;
}
export { useDetailContainerContext };
export default DetailContainerContext;
