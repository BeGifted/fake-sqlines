package util;

import com.alibaba.druid.sql.SQLTransformUtils;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.util.FnvHash;

import java.util.List;
/**
 * @author gongyuandaye
 * @date 2022/4/26 21:50
 */
public class DataTypeTranslator extends SQLTransformUtils {

    public static SQLDataType DataTypeTranslator(SQLDataType x) {
        System.out.println("inDataType：" + x.getName());
        final String name = x.getName();
        final long nameHash = x.nameHashCode64();
        if (name == null) {
            return x;
        }
        List<SQLExpr> argumentns = x.getArguments();
        SQLDataType dataType;
        if (nameHash == FnvHash.Constants.UROWID) {
            int len = 4000;
            if (argumentns.size() == 1) {
                SQLExpr arg0 = argumentns.get(0);
                if (arg0 instanceof SQLIntegerExpr) {
                    len = ((SQLIntegerExpr) arg0).getNumber().intValue();
                }
            }
            dataType = new SQLDataTypeImpl("VARCHAR", len);
        } else if (nameHash == FnvHash.Constants.ROWID) {
            dataType = new SQLDataTypeImpl("CHAR", 10);

        } else if (nameHash == FnvHash.Constants.BOOLEAN) {
            dataType = new SQLDataTypeImpl("TINYINT");

        } else if (nameHash == FnvHash.Constants.INTEGER) {
            dataType = new SQLDataTypeImpl("INT");

        } else if (nameHash == FnvHash.Constants.FLOAT
                || nameHash == FnvHash.Constants.BINARY_FLOAT) {
            dataType = new SQLDataTypeImpl("FLOAT");

        } else if (nameHash == FnvHash.Constants.REAL
                || nameHash == FnvHash.Constants.BINARY_DOUBLE
                || nameHash == FnvHash.Constants.DOUBLE_PRECISION) {
            dataType = new SQLDataTypeImpl("DOUBLE");

        } else if (nameHash == FnvHash.Constants.NUMBER) {
            if (argumentns.size() == 0) {
                dataType = new SQLDataTypeImpl("DECIMAL", 38);
            } else {
                SQLExpr arg0 = argumentns.get(0);

                int precision, scale = 0;
                if (arg0 instanceof SQLAllColumnExpr) {
                    precision = 9;
                } else {
                    precision = ((SQLIntegerExpr) arg0).getNumber().intValue();
                }

                if (argumentns.size() > 1) {
                    scale = ((SQLIntegerExpr) argumentns.get(1)).getNumber().intValue();
                }

                if (scale > precision) {
                    if (arg0 instanceof SQLAllColumnExpr) {
                        precision = 19;
                        if (scale > precision) {
                            precision = scale;
                        }
                    } else {
                        precision = scale;
                    }
                }

                if (scale == 0) {
                    if (precision < 3) {
                        dataType = new SQLDataTypeImpl("TINYINT");
                    } else if (precision < 5) {
                        dataType = new SQLDataTypeImpl("SMALLINT");
                    } else if (precision < 9) {
                        dataType = new SQLDataTypeImpl("INT");
                    } else if (precision < 19) {
                        dataType = new SQLDataTypeImpl("BIGINT");
                    } else {
                        dataType = new SQLDataTypeImpl("DECIMAL", precision);
                    }
                } else {
                    dataType = new SQLDataTypeImpl("DECIMAL", precision, scale);
                }
            }
        } else if (nameHash == FnvHash.Constants.DEC || nameHash == FnvHash.Constants.DECIMAL) {
            dataType = x.clone();
            dataType.setName("DECIMAL");

            int precision = 0;
            if (argumentns.size() > 0) {
                precision = ((SQLIntegerExpr) argumentns.get(0)).getNumber().intValue();
            }

            int scale = 0;
            if (argumentns.size() > 1) {
                scale = ((SQLIntegerExpr) argumentns.get(1)).getNumber().intValue();
                if (precision < scale) {
                    ((SQLIntegerExpr) dataType.getArguments().get(1)).setNumber(precision);
                }
            }
        } else if (nameHash == FnvHash.Constants.RAW) {
            int len;
            if (argumentns.size() == 0) {
                len = -1;
            } else if (argumentns.size() == 1) {
                SQLExpr arg0 = argumentns.get(0);
                if (arg0 instanceof SQLNumericLiteralExpr) {
                    len = ((SQLNumericLiteralExpr) arg0).getNumber().intValue();
                } else {
                    throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
                }
            } else {
                throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
            }

            if (len == -1) {
                dataType = new SQLDataTypeImpl("BINARY");
            } else if (len <= 255) {
                dataType = new SQLDataTypeImpl("BINARY", len);
            } else {
                dataType = new SQLDataTypeImpl("VARBINARY", len);
            }
        } else if (nameHash == FnvHash.Constants.CHAR || nameHash == FnvHash.Constants.CHARACTER) {
            if (argumentns.size() == 1) {
                SQLExpr arg0 = argumentns.get(0);

                int len;
                if (arg0 instanceof SQLNumericLiteralExpr) {
                    len = ((SQLNumericLiteralExpr) arg0).getNumber().intValue();
                } else {
                    throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
                }

                if (len <= 255) {
                    dataType = new SQLCharacterDataType("CHAR", len);
                } else {
                    dataType = new SQLCharacterDataType("VARCHAR", len);
                }
            } else if (argumentns.size() == 0) {
                dataType = new SQLCharacterDataType("CHAR");
            } else {
                throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
            }

        } else if (nameHash == FnvHash.Constants.NCHAR) {
            if (argumentns.size() == 1) {
                SQLExpr arg0 = argumentns.get(0);

                int len;
                if (arg0 instanceof SQLNumericLiteralExpr) {
                    len = ((SQLNumericLiteralExpr) arg0).getNumber().intValue();
                } else {
                    throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
                }

                if (len <= 255) {
                    dataType = new SQLCharacterDataType("NCHAR", len);
                } else {
                    dataType = new SQLCharacterDataType("NVARCHAR", len);
                }
            } else if (argumentns.size() == 0) {
                dataType = new SQLCharacterDataType("NCHAR");
            } else {
                throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
            }

        } else if (nameHash == FnvHash.Constants.VARCHAR2) {
            if (argumentns.size() > 0) {
                int len;
                SQLExpr arg0 = argumentns.get(0);
                if (arg0 instanceof SQLNumericLiteralExpr) {
                    len = ((SQLNumericLiteralExpr) arg0).getNumber().intValue();
                } else {
                    throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
                }
                if (len >= 4000) {
                    dataType = new SQLCharacterDataType("TEXT");
                } else {
                    dataType = new SQLCharacterDataType("VARCHAR", len);
                }
            } else {
                dataType = new SQLCharacterDataType("VARCHAR");
            }

        } else if (nameHash == FnvHash.Constants.NVARCHAR2) {
            if (argumentns.size() > 0) {
                int len;
                SQLExpr arg0 = argumentns.get(0);
                if (arg0 instanceof SQLNumericLiteralExpr) {
                    len = ((SQLNumericLiteralExpr) arg0).getNumber().intValue();
                } else {
                    throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
                }
                dataType = new SQLCharacterDataType("NVARCHAR", len);
            } else {
                dataType = new SQLCharacterDataType("NVARCHAR");
            }

        } else if (nameHash == FnvHash.Constants.BFILE) {
            dataType = new SQLCharacterDataType("VARCHAR", 255);

        } else if (nameHash == FnvHash.Constants.DATE
                || nameHash == FnvHash.Constants.TIMESTAMP) {
            int len = -1;
            if (argumentns.size() > 0) {
                SQLExpr arg0 = argumentns.get(0);
                if (arg0 instanceof SQLNumericLiteralExpr) {
                    len = ((SQLNumericLiteralExpr) arg0).getNumber().intValue();
                } else {
                    throw new UnsupportedOperationException(SQLUtils.toOracleString(x));
                }
            }
            dataType = new SQLDataTypeImpl("DATETIME");
        } else if (nameHash == FnvHash.Constants.BLOB
                || nameHash == FnvHash.Constants.LONG_RAW) {
            argumentns.clear();
            dataType = new SQLDataTypeImpl("LONGBLOB");
        } else if (nameHash == FnvHash.Constants.CLOB
                || nameHash == FnvHash.Constants.NCLOB
                || nameHash == FnvHash.Constants.LONG
                || nameHash == FnvHash.Constants.XMLTYPE) {
            argumentns.clear();
            dataType = new SQLCharacterDataType("LONGTEXT");
        } else {
            dataType = x;
        }
        if (dataType != x) {
            dataType.setParent(x.getParent());
        }

        System.out.println("outDataType：" + dataType.getName());
        return dataType;
    }

}
