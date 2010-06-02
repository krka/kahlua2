package se.krka.kahlua.vm;

public class KahluaArray implements KahluaTable {

	private KahluaTable metatable;
	
	private Object[] data;
	private int len;

    private boolean recalculateLen;

    public KahluaArray() {
		data = new Object[16];
		len = 0;
	}
	
	public int len() {
        if (recalculateLen) {
            int index = this.len - 1;
            Object[] data = this.data;
            while (index >= 0) {
                if (data[index] != null) {
                    break;
                }
                index--;
            }
            this.len = index + 1;
            recalculateLen = false;
        }
		return len;
	}

    public KahluaTableIterator iterator() {
        return new KahluaTableIterator() {
            private Double curKey;
            private Object curValue;
            private int index = 1;
            public int call(LuaCallFrame callFrame, int nArguments) {
                if (advance()) {
                    return callFrame.push(getKey(), getValue());
                }
                return 0;
            }

            public boolean advance() {
                while (true) {
                    if (index > len()) {
                        return false;
                    }
                    Object value = rawget(index);
                    if (value != null) {
                        int localIndex = index;
                        index++;
                        curKey = KahluaUtil.toDouble(localIndex);
                        curValue = value;
                        return true;
                    }
                    index++;
                }
            }

            public Object getKey() {
                return curKey;
            }

            public Object getValue() {
                return curValue;
            }
        };
    }

	public boolean isEmpty() {
		return len() == 0;
	}

	public Object rawget(int index) {
        if (index < 1 || index > len) {
            return null;
        }
        return data[(index - 1)];
    }

    public void rawset(int index, Object value) {
        if (index <= 0) {
            KahluaUtil.fail("Index out of range: " + index);
        }
        if (index >= len) {
	        if (value == null) {
	        	if (index == len) {
	                data[index - 1] = value;
	                recalculateLen = true;
	        	}
	        	return;
	        }
            if (data.length < index) {
                int newMaxLen = 2 * index;
                int newCap = newMaxLen - 1;
                Object[] newData = new Object[newCap];
                System.arraycopy(data, 0, newData, 0, len);
                data = newData;
            }
            len = index;
        }
        data[(index - 1)] = value;
    }

    private int getKeyIndex(Object key) {
        if (key instanceof Double) {
            Double d = (Double) key;
            return d.intValue();
        }
        return -1;
    }

    public Object rawget(Object key) {
        int index = getKeyIndex(key);
        return rawget(index);
    }

    public void rawset(Object key, Object value) {
        int index = getKeyIndex(key);
        if (index == -1) {
            KahluaUtil.fail("Invalid table key: " + key);
        }
        rawset(index, value);
    }

    public Object next(Object key) {
		int index;
		if (key == null) {
			index = 0;
		} else {
            index = getKeyIndex(key);
            if (index <= 0 || index > len) {
                KahluaUtil.fail("invalid key to 'next'");
                return null;
            }
		}
		while (index < len) {
			if (data[index] != null) {
				return KahluaUtil.toDouble(index + 1);
			}
			index++;
		}
		return null;
	}

    public KahluaTable getMetatable() {
		return metatable;
	}

	public void setMetatable(KahluaTable metatable) {
		this.metatable = metatable;
	}
}
