package com.gigigenie.domain.chat.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VectorType implements UserType<List<Float>> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<List<Float>> returnedClass() {
        return (Class<List<Float>>) (Class<?>) List.class;
    }

    @Override
    public boolean equals(List<Float> x, List<Float> y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(List<Float> x) {
        return Objects.hashCode(x);
    }

    @Override
    public List<Float> nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (rs.wasNull() || value == null) {
            return null;
        }

        value = value.replace("[", "").replace("]", "");
        String[] elements = value.split(",");
        List<Float> result = new ArrayList<>(elements.length);

        for (String element : elements) {
            result.add(Float.parseFloat(element.trim()));
        }

        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, List<Float> value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < value.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(value.get(i));
        }
        sb.append("]");

        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(sb.toString());

        st.setObject(index, pgObject);
    }

    @Override
    public List<Float> deepCopy(List<Float> value) {
        if (value == null) {
            return null;
        }
        return new ArrayList<>(value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(List<Float> value) {
        if (value == null) {
            return null;
        }
        Float[] array = value.toArray(new Float[0]);
        return array;
    }

    @Override
    public List<Float> assemble(Serializable cached, Object owner) {
        if (cached == null) {
            return null;
        }
        Float[] array = (Float[]) cached;
        return Arrays.asList(array);
    }

    @Override
    public List<Float> replace(List<Float> original, List<Float> target, Object owner) {
        return deepCopy(original);
    }
}