import { useCallback, useEffect, useState } from 'react'

import { getFeaturedRooms } from '../api/rooms.service'
import type { RoomListing } from '../types'

type State =
  | { status: 'loading'; rooms: RoomListing[] }
  | { status: 'success'; rooms: RoomListing[] }
  | { status: 'error'; rooms: RoomListing[] }

export function useFeaturedRooms() {
  const [state, setState] = useState<State>({ status: 'loading', rooms: [] })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let cancelled = false

    getFeaturedRooms()
      .then((rooms) => {
        if (!cancelled) setState({ status: 'success', rooms })
      })
      .catch(() => {
        if (!cancelled) setState({ status: 'error', rooms: [] })
      })

    return () => {
      cancelled = true
    }
  }, [attempt])

  const retry = useCallback(() => {
    setState({ status: 'loading', rooms: [] })
    setAttempt((n) => n + 1)
  }, [])

  return { ...state, retry }
}
